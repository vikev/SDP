package sdp.pc.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import sdp.pc.common.Constants;

import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.StateException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

/**
 * This code builds a JFrame that shows the feed from the camera and calculates
 * and displays positions of objects on the field. Part of the code is inspired
 * from group 1 of SDP 2013. Most important method is processImage - don't touch
 * stuff like initGui unless need be
 * 
 * @author Borislav Ikonomov, Group 8, SDP 2014
 * 
 */
public class Vision extends WindowAdapter implements CaptureCallback {

	// Camera and image parameters
	private static final int WIDTH = 640, HEIGHT = 480,
			VIDEO_STANDARD = V4L4JConstants.STANDARD_PAL, CHANNEL = 0,
			X_FRAME_OFFSET = 1, Y_FRAME_OFFSET = 25;
	private static final String DEVICE = "/dev/video0";
	private VideoDevice videoDevice;
	private FrameGrabber frameGrabber;

	private static final int PLAYER_RADIUS = 18;
	private static final double VECTOR_THRESHOLD = 3.0;

	private JLabel label;
	private JFrame frame;
	private static Color[][] rgb = new Color[700][520];
	private static float[][][] hsb = new float[700][520][3];
	private static float[] cHsb;
	private static ArrayList<Point2> pitchPoints = new ArrayList<Point2>();
	private static WorldState state = new WorldState();
	long initialTime; // For FPS calculation

	// Used for normalisation (first float is max brightness, second is min
	// brightness)
	private static float[] minMaxBrightness = { 1.0f, 0.0f };
	// Used to denote if video feed has been pre-processed
	private static int preprocessed = 0; // 'Int' so that system can wait a few
											// frames first
	// Used for calculating direction the ball is heading
	static Point2[] prevFramePos = new Point2[10];

	/**
	 * Provides Java application support. On launch, runs a JFrame window which
	 * displays the video feed with our overlaid data.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		// Initialise prevFramePos:
		for (int i = 0; i < 10; i++)
			prevFramePos[i] = new Point2(0, 0);

		// Begin:
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new Vision(state);
				} catch (V4L4JException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Builds a WebcamViewer object
	 * 
	 * @param state
	 *            - the WorldState which will be updated with all the
	 *            information from the vision system dynamically.
	 * 
	 * @throws V4L4JException
	 *             if any parameter if invalid
	 */
	public Vision(WorldState state) throws V4L4JException {
		try {
			initFrameGrabber();
		} catch (V4L4JException e1) {
			System.err.println("Error setting up capture");
			e1.printStackTrace();
			cleanupCapture();
			return;
		}

		initGUI();

		frameGrabber.setCaptureCallback(new CaptureCallback() {
			public void exceptionReceived(V4L4JException e) {
				System.err.println("Unable to capture frame:");
				e.printStackTrace();
			}

			public void nextFrame(VideoFrame frame) {
				initialTime = System.currentTimeMillis(); // for FPS
				BufferedImage frameImage = frame.getBufferedImage();
				frame.recycle();
				if (preprocessed < 25)
					preprocessed++;
				else if (preprocessed == 25)
					preprocess(frameImage);
				else
					processImage(frameImage);
			}
		});

		frameGrabber.startCapture();
	}

	private static void preprocess(BufferedImage image) {
		preprocessed++;

		ArrayList<Point2> whitePoints = new ArrayList<Point2>();
		// Find borders
		for (int row = Constants.TABLE_MIN_Y; row < Constants.TABLE_MAX_Y; row++) {
			for (int column = Constants.TABLE_MIN_X; column < Constants.TABLE_MAX_X; column++) {

				// Color pixelColorRGB = new Color(image.getRGB(column, row));
				Color pixelColorRGB = normaliseColor(image, row, column);
				Point2 p = new Point2(column, row);
				if (isWhite(pixelColorRGB, cHsb) && row < 470) {
					whitePoints.add(p);
				}
			}
		}

		LinkedList<Point2> borders = Alg.convexHull(whitePoints);
		for (int row = Constants.TABLE_MIN_Y; row < Constants.TABLE_MAX_Y; row++) {
			for (int column = Constants.TABLE_MIN_X; column < Constants.TABLE_MAX_X; column++) {
				Point2 p = new Point2(column, row);
				if (Alg.isInHull(borders, p))
					pitchPoints.add(p);
			}
		}

		minMaxBrightness = findMinMaxBrigthness(image);
	}

	/**
	 * Identifies objects of interest (ball, robots) in the image while in play.
	 * The information from it (positions, orientations, predictions) will be
	 * passed to a WorldState object (called state) to be used by other files.
	 * 
	 * @param image
	 *            the image to process
	 */
	private void processImage(BufferedImage image) {

		// Initialise color recognition values
		int ballCounter = 0;
		Point2 ballPos = new Point2();

		int yellowLeftCounter = 0;
		Point2 yellowLeftPos = new Point2();
		ArrayList<Point2> yellowLeftPoints = new ArrayList<Point2>();

		int blueLeftCounter = 0;
		Point2 blueLeftPos = new Point2();
		ArrayList<Point2> blueLeftPoints = new ArrayList<Point2>();

		int yellowRightCounter = 0;
		Point2 yellowRightPos = new Point2();
		ArrayList<Point2> yellowRightPoints = new ArrayList<Point2>();

		int blueRightCounter = 0;
		Point2 blueRightPos = new Point2();
		ArrayList<Point2> blueRightPoints = new ArrayList<Point2>();

		// Loop through all table values, recognizing pixel regions as necessary
		for (Point2 p : pitchPoints) {
			int column = p.getX();
			int row = p.getY();

			// Color pixelColorRGB = new Color(image.getRGB(column, row));
			Color pixelColorRGB = normaliseColor(image, row, column);

			// Find "Ball" pixels
			if (isBall(pixelColorRGB, cHsb)) {
				ballPos = ballPos.add(p);
				ballCounter++;
				// Makes red pixels orange - for debugging
				// image.setRGB(column, row, Color.ORANGE.getRGB());
			}

			// Find Yellow pixels
			if (isYellow(pixelColorRGB, cHsb)) {
				if (column < Constants.TABLE_CENTRE_X) {
					yellowLeftPos = yellowLeftPos.add(p);
					yellowLeftCounter++;
					yellowLeftPoints.add(new Point2(column, row));

					// Makes yellow pixels orange
					image.setRGB(column, row, Color.ORANGE.getRGB());
				} else {
					yellowRightPos = yellowRightPos.add(p);
					yellowRightCounter++;
					yellowRightPoints.add(new Point2(column, row));

					// Makes yellow pixels orange
					image.setRGB(column, row, Color.ORANGE.getRGB());
				}
			}

			// Find Blue pixels
			if (isBlue(pixelColorRGB, cHsb)) {
				if (column < Constants.TABLE_CENTRE_X) {
					blueLeftPos = blueLeftPos.add(p);
					blueLeftCounter++;
					blueLeftPoints.add(new Point2(column, row));

					// Makes blue pixels more blue
					image.setRGB(column, row, Color.BLUE.getRGB());
				} else {
					blueRightPos = blueRightPos.add(p);
					blueRightCounter++;
					blueRightPoints.add(new Point2(column, row));

					// Makes blue pixels more blue
					image.setRGB(column, row, Color.BLUE.getRGB());
				}
			}
		}

		// Get average position of ball
		if (ballCounter > 0)
			ballPos = ballPos.div(ballCounter);

		// Get average position of left yellow bot
		if (yellowLeftCounter > 0) {
			yellowLeftPos = yellowLeftPos.div(yellowLeftCounter);
			ArrayList<Point2> newYellow = Point2.removeOutliers(
					yellowLeftPoints, yellowLeftPos);
			yellowLeftPos.filterPoints(newYellow);
		}

		// Get average position of left blue bot
		if (blueLeftCounter > 0) {
			blueLeftPos = blueLeftPos.div(blueLeftCounter);
			ArrayList<Point2> newBlue = Point2.removeOutliers(blueLeftPoints,
					blueLeftPos);
			blueLeftPos.filterPoints(newBlue);
		}

		// Get average position of right yellow bot
		if (yellowRightCounter > 0) {
			yellowRightPos = yellowRightPos.div(yellowRightCounter);
			ArrayList<Point2> newYellow2 = Point2.removeOutliers(
					yellowRightPoints, yellowRightPos);
			yellowRightPos.filterPoints(newYellow2);
		}

		// Get average position of right blue bot
		if (blueRightCounter > 0) {
			blueRightPos = blueRightPos.div(blueRightCounter);
			ArrayList<Point2> newBlue2 = Point2.removeOutliers(blueRightPoints,
					blueRightPos);
			blueRightPos.filterPoints(newBlue2);
		}

		// Calculates where ball is going
		Point2 avgPrevPos = new Point2(0, 0);
		for (Point2 p : prevFramePos)
			avgPrevPos = avgPrevPos.add(p);
		avgPrevPos = avgPrevPos.div(prevFramePos.length);
		avgPrevPos = avgPrevPos.subtract(ballPos).mult(-5).add(ballPos);

		// TODO: fix orientation code
		double yellowOrientation = 0;

		// Point2 blackPos = new Point2();
		Point2 blackPos = findBlackDot(image, yellowLeftPos);
		blackPos = blackPos.subtract(yellowLeftPos).mult(-5).add(yellowLeftPos);

		// Update World State
		state.setBallPosition(ballPos);
		state.setRobotPosition(0, 0, yellowLeftPos);
		state.setRobotFacing(0, 0, yellowOrientation);

		// Create graphical representation
		Graphics imageGraphics = image.getGraphics();
		Graphics frameGraphics = label.getGraphics();

		// Ball location (and direction)
		if (Alg.pointInPitch(ballPos)) {
			imageGraphics.setColor(Color.red);
			imageGraphics.drawLine(0, ballPos.getY(), 640, ballPos.getY());
			imageGraphics.drawLine(ballPos.getX(), 0, ballPos.getX(), 480);
			if (Alg.lineSize(ballPos, avgPrevPos) > VECTOR_THRESHOLD) {
				imageGraphics.drawLine(ballPos.getX(), ballPos.getY(),
						avgPrevPos.getX(), avgPrevPos.getY());
			}
		}

		// Yellow robots locations
		if (Alg.pointInPitch(yellowLeftPos)) {
			drawCircle(yellowLeftPos, imageGraphics, Constants.YELLOW_BLEND,
					Constants.ROBOT_CIRCLE_RADIUS);
			findOrientation(yellowLeftPos, imageGraphics);
		}

		if (Alg.pointInPitch(yellowRightPos)) {
			drawCircle(yellowRightPos, imageGraphics, Constants.YELLOW_BLEND,
					Constants.ROBOT_CIRCLE_RADIUS);
			findOrientation(yellowRightPos, imageGraphics);
		}

		// draw orientation (temp)
		// if (Alg.pointInPitch(yellowLeftPos)) {
		// imageGraphics.drawLine(yellowLeftPos.getX(), yellowLeftPos.getY(),
		// blackPos.getX(), blackPos.getY());
		// }

		// TODO: Implement blackpos for all bots

		// Blue robots locations
		if (Alg.pointInPitch(blueLeftPos)) {
			drawCircle(blueLeftPos, imageGraphics, Constants.BLUE_BLEND,
					Constants.ROBOT_CIRCLE_RADIUS);
			findOrientation(blueLeftPos,imageGraphics);
		}

		if (Alg.pointInPitch(blueRightPos)) {
			drawCircle(blueRightPos, imageGraphics, Constants.BLUE_BLEND,
					Constants.ROBOT_CIRCLE_RADIUS);
			findOrientation(blueRightPos,imageGraphics);
		}

		// Draw centre line
		imageGraphics.setColor(new Color(1.0f, 1.0f, 1.0f, 0.3f));
		imageGraphics.drawLine(Constants.TABLE_CENTRE_X,
				Constants.TABLE_MIN_Y + 1, Constants.TABLE_CENTRE_X,
				Constants.TABLE_MAX_Y - 1);

		// Draw top and bottom line as well
		// imageGraphics.drawLine(Constants.TABLE_MIN_X, Constants.TABLE_MIN_Y,
		// Constants.TABLE_MAX_X, Constants.TABLE_MIN_Y);
		// imageGraphics.drawLine(Constants.TABLE_MIN_X, Constants.TABLE_MAX_Y,
		// Constants.TABLE_MAX_X, Constants.TABLE_MAX_Y);

		// Saves this frame's ball position and shifts previous frames'
		// positions
		for (int i = prevFramePos.length - 1; i > 0; i--) {
			prevFramePos[i] = prevFramePos[i - 1];
		}
		prevFramePos[0] = ballPos;

		// Display the FPS that the vision system is running at
		long after = System.currentTimeMillis(); // Used to calculate the FPS.
		float fps = (1.0f) / ((after - initialTime) / 1000.0f);
		imageGraphics.setColor(Color.white);
		imageGraphics.drawString("FPS: " + (int) fps, 15, 15);
		int x = 1, y = 0;
		java.awt.Point pos;

		// Draw mouse position, RGB, and HSB values to screen
		pos = frame.getMousePosition();
		if (pos != null) {
			x = (int) Math.round(pos.getX()) - X_FRAME_OFFSET;
			y = (int) Math.round(pos.getY()) - Y_FRAME_OFFSET;
			if (x > 0 && x <= (WIDTH - X_FRAME_OFFSET)
					&& y <= (HEIGHT - Y_FRAME_OFFSET)) {

				imageGraphics.drawString("Mouse pos: x:" + x + " y:" + y, 15,
						30);

				Color c = new Color(image.getRGB(x, y));

				imageGraphics.drawString(
						"Color: R:" + c.getRed() + " G:" + c.getGreen() + " B:"
								+ c.getBlue(), 15, 45);
				float[] hsb = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsb);
				imageGraphics.drawString(
						"HSB: H:" + new DecimalFormat("#.###").format(hsb[0])
								+ " S:"
								+ new DecimalFormat("#.###").format(hsb[1])
								+ " B:"
								+ new DecimalFormat("#.###").format(hsb[2]),
						15, 60);
			}
		}

		// Finally draw the image to screen
		frameGraphics.drawImage(image, 0, 0, WIDTH, HEIGHT, null);
	}

	private double findOrientation(Point2 centroid, Graphics gfx) {
		double angBest = 0.0;
		if (Alg.pointInPitch(centroid)) {
			double r = 0;
			double brBest = 255.0;
			for (int i = 0; i < Constants.HEAD_ARC_FIDELITY; i++) {
				r += 2.0 * Math.PI / Constants.HEAD_ARC_FIDELITY;
				int row = centroid.getY()
						+ (int) (Constants.HEAD_ENUM_RADIUS * Math.sin(r));
				int column = centroid.getX()
						+ (int) (Constants.HEAD_ENUM_RADIUS * Math.cos(r));
				if (hsb[column][row][2] < brBest) {
					brBest = hsb[column][row][2];
					angBest = r;
				}
			}
			Point2 pt = new Point2(centroid.getX()
					+ (int) (Constants.HEAD_ENUM_RADIUS * Math.cos(angBest)),
					centroid.getY()
							+ (int) (Constants.HEAD_ENUM_RADIUS * Math
									.sin(angBest)));
			drawCircle(pt, gfx, Constants.GRAY_BLEND,
					Constants.ROBOT_HEAD_RADIUS);
		}
		return (angBest+Math.PI)*180.0/Math.PI;
	}

	private void drawCircle(Point2 centrePt, Graphics gfx, Color c, int radius) {
		gfx.setColor(c);
		gfx.drawOval(centrePt.getX() - radius, centrePt.getY() - radius,
				radius * 2, radius * 2);
	}

	private static Color normaliseColor(BufferedImage image, int row, int column) {
		// Normalise color values:
		// Update RGB handle
		Color pixelColorRGB = new Color(image.getRGB(column, row));
		rgb[column][row] = pixelColorRGB;

		// Update HSB handle
		cHsb = hsb[column][row];
		Color.RGBtoHSB(pixelColorRGB.getRed(), pixelColorRGB.getGreen(),
				pixelColorRGB.getBlue(), cHsb);

		// Scale the values of the pitch
		float br = hsb[column][row][2];

		// hsb is of the form {max, min}
		br = (br - minMaxBrightness[1]) / minMaxBrightness[0];
		image.setRGB(column, row,
				Color.HSBtoRGB(hsb[column][row][0], hsb[column][row][1], br));

		for (int i = 0; i < 3; i++)
			cHsb[i] *= 255;

		hsb[column][row] = cHsb;
		return pixelColorRGB;
	}

	/**
	 * Gets the position of the black dot around some point Used to determine a
	 * robot's orientation, given its centre point
	 * 
	 * @param colorCenter
	 *            the center of the robot's yellow/blue
	 */
	private Point2 findBlackDot(BufferedImage i, Point2 colorCenter) {
		// bounding rect to search for black pixels
		int xs = Math.max(0, colorCenter.getX() - PLAYER_RADIUS);
		int ys = Math.max(0, colorCenter.getY() - PLAYER_RADIUS);
		int xe = Math.min(WIDTH, colorCenter.getX() + PLAYER_RADIUS);
		int ye = Math.min(HEIGHT, colorCenter.getY() + PLAYER_RADIUS);

		// get all the black points in the bounding rect
		ArrayList<Point2> pts = new ArrayList<Point2>();
		float[] cHsb;
		float[] midHsb = new float[3];
		Color cRgb;
		for (int ix = xs; ix < xe; ix++)
			for (int iy = ys; iy < ye; iy++) {
				// get colors
				cHsb = hsb[ix][iy];
				cRgb = rgb[ix][iy];

				// record average
				midHsb[0] += cHsb[0];
				midHsb[1] += cHsb[1];
				midHsb[2] += cHsb[2];

				// add if "green"
				if (isGreen(cRgb, cHsb)) {
					pts.add(new Point2(ix, iy));
					i.setRGB(ix, iy, Color.pink.getRGB());
				}
			}

		// do k-means
		Cluster c = Kmeans.doKmeans(pts, new Point2(colorCenter))[0]; // only 1
																		// cluster

		if (pts.size() == 0)
			return new Point2(colorCenter);

		@SuppressWarnings("unused")
		List<Point2> hull = Alg.convexHull(pts);

		// TODO:
		// while points > some_treshold and rect > some_rect:
		// rect /= 2;
		// pts = pointsIn(rect)
		// c = Kmeans(pts)

		return c.getMean();
	}

	/**
	 * Checks if a pixel is black, i.e. can be used for orientation detection
	 * 
	 * @param rgb
	 * @param hsb
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isBlack(Color rgb, float[] hsb) {
		return 0.6 < hsb[0] && hsb[0] < 0.67 && hsb[1] > 0.5;
	}

	private static boolean isWhite(Color rgb, float[] hsb) {
		return hsb[2] > 125 && rgb.getGreen() < 150;
	}

	private boolean isGreen(Color rgb, float[] hsb) {
		return 0.2f < hsb[1] && hsb[1] < 0.35f && 0.25f < hsb[2]
				&& hsb[2] < 0.45f && rgb.getRed() < 80 && rgb.getBlue() < 80
				&& rgb.getGreen() < 80;
	}

	/**
	 * Determines if a pixel is part of the ball, based on input RGB colours and
	 * hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the ball), false otherwise.
	 */
	private boolean isBall(Color c, float[] hsb) {
		return (c.getRed() > 125 && c.getGreen() < 50 && c.getBlue() < 50 && hsb[1] > 140);
		// Sat > 140
	}

	/**
	 * Determines if a pixel is part of the yellow T, based on input RGB colours
	 * and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the yellow T), false otherwise.
	 */
	private boolean isYellow(Color c, float[] hsb) {
		return (c.getRed() > 130 && c.getGreen() > 70 && c.getGreen() < 120 && c
				.getBlue() < 40);
	}

	/**
	 * Finds the brightest and darkest points on the image
	 * 
	 * @param image
	 *            The Image to be used for min max brightness foundation
	 * 
	 * @return The list of two float HSB values representing the brightest and
	 *         the darkest values
	 */
	private static float[] findMinMaxBrigthness(BufferedImage image) {
		float maxBr = 0.0f;
		float minBr = 1.0f;

		/*
		 * for (int row = Constants.TABLE_MIN_Y; row < Constants.TABLE_MAX_Y;
		 * row++) { for (int column = Constants.TABLE_MIN_X; column <
		 * Constants.TABLE_MAX_X; column++) {
		 */
		for (Point2 p : pitchPoints) {
			int column = p.getX();
			int row = p.getY();
			Color change = new Color(image.getRGB(column, row));
			float[] cHsb = hsb[column][row];
			Color.RGBtoHSB(change.getRed(), change.getBlue(),
					change.getGreen(), cHsb);
			float br = hsb[column][row][2];
			if (br >= maxBr) {
				maxBr = br;
			}

			if (br <= minBr) {
				minBr = br;
			}
		}
		return new float[] { maxBr, minBr };
	}

	/**
	 * Determines if a pixel is part of the blue T, based on input RGB colours
	 * and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the blue T), false otherwise.
	 */
	private boolean isBlue(Color c, float[] hsb) {
		return (c.getRed() > 0 && c.getRed() < 50 && c.getGreen() > 50
				&& c.getGreen() < 100 && c.getBlue() > 70 && c.getBlue() < 150
				&& 110 < hsb[0] && hsb[0] < 140 && hsb[1] > 120 && 70 < hsb[2] && hsb[2] < 100);
		// 110 < Hue < 140 ; Sat > 120 ; 70 < Val < 100;
	}

	double prevBestAngle = 0;

	/**
	 * Finds the orientation of a robot, given a list of the points contained
	 * within it's T-shape (in terms of a list of x coordinates and y
	 * coordinates), the mean x and y coordinates, and the image from which it
	 * was taken.
	 * 
	 * @param xpoints
	 *            The x-coordinates of the points contained within the T-shape.
	 * @param ypoints
	 *            The y-coordinates of the points contained within the T-shape.
	 * @param meanX
	 *            The mean x-point of the T.
	 * @param meanY
	 *            The mean y-point of the T.
	 * @param image
	 *            The image from which the points were taken.
	 * @param showImage
	 *            A boolean flag - if true a line will be drawn showing the
	 *            direction of orientation found.
	 * 
	 * @return An orientation from -Pi to Pi degrees.
	 * @throws NoAngleException
	 */

	/**
	 * Initialises a FrameGrabber object with the given parameters.
	 * 
	 * @param videoDevice
	 *            The video device file to capture from.
	 * @param inWidth
	 *            The desired capture width.
	 * @param inHeight
	 *            The desired capture height.
	 * @param CHANNEL
	 *            The capture channel.
	 * @param videoStandard
	 *            The capture standard.
	 * @param compressionQuality
	 *            The JPEG compression quality.
	 * 
	 * @throws V4L4JException
	 *             If any parameter is invalid.
	 */
	private void initFrameGrabber() throws V4L4JException {
		videoDevice = new VideoDevice(DEVICE);
		frameGrabber = videoDevice.getJPEGFrameGrabber(WIDTH, HEIGHT, CHANNEL,
				VIDEO_STANDARD, 80);
		frameGrabber.setCaptureCallback(this);
		System.out.println("Starting capture at " + WIDTH + "x" + HEIGHT);
	}

	/**
	 * Creates the UI components (the JFrame) and initialises them
	 */
	private void initGUI() {
		frame = new JFrame();
		label = new JLabel();
		frame.getContentPane().add(label);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(this);
		frame.setVisible(true);
		frame.setSize(WIDTH, HEIGHT);
	}

	/**
	 * This method stops the capture and releases the frame grabber and video
	 * device
	 */
	private void cleanupCapture() {
		try {
			frameGrabber.stopCapture();
		} catch (StateException ex) {
		}
		videoDevice.releaseFrameGrabber();
		videoDevice.release();
	}

	/**
	 * Catch window closing event so we can free up resources before exiting
	 * 
	 * @param e
	 */
	public void windowClosing(WindowEvent e) {
		cleanupCapture();
		frame.dispose();
	}

	@Override
	public void exceptionReceived(V4L4JException e) {
		e.printStackTrace();
	}

	@Override
	public void nextFrame(VideoFrame frame) {
		label.getGraphics().drawImage(frame.getBufferedImage(), 0, 0, WIDTH,
				HEIGHT, null);
		frame.recycle();
	}
}