package sdp.pc.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

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
	
	private static final double VECTOR_THRESHOLD = 3.0;
	
	private VideoDevice videoDevice;
	private FrameGrabber frameGrabber;
	private JLabel label;
	private JFrame frame;
	private static WorldState state = new WorldState();
	long initialTime; // For FPS calculation

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
				processImage(frameImage);
			}
		});

		frameGrabber.startCapture();

	}

	Color[][] rgb = new Color[700][520];
	float[][][] hsb = new float[700][520][3];

	/**
	 * Checks whether a given point is in the pitch
	 */
	private static boolean pointInPitch(Point2 q) {
		int x = q.getX();
		int y = q.getY();
		if (x < Constants.TABLE_MAX_X && x > Constants.TABLE_MIN_X
				&& y < Constants.TABLE_MAX_Y && y > Constants.TABLE_MIN_Y) {
			return true;
		}
		return false;
	}

	/**
	 * Calculates the length of a line between two points
	 */
	private static double lineSize(Point2 a, Point2 b) {
		double s = (double) (b.getX() - a.getX());
		double t = (double) (b.getY() - b.getY());
		return Math.sqrt(s * s + t * t);
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

		int ballN = 0;
		Point2 ballPos = new Point2();
		int yellowN = 0;
		Point2 yellowPos = new Point2();
		int blueN = 0;
		Point2 bluePos = new Point2();
		ArrayList<Point2> yellowPoints = new ArrayList<Point2>();
		ArrayList<Point2> bluePoints = new ArrayList<Point2>();
		float[] minMaxBrigthness = findMinMaxBrigthness(image);

		// Both loops need to start from table edges rather than
		for (int row = 80; row < image.getHeight() - 80; row++) {
			for (int column = 50; column < image.getWidth() - 55; column++) {

				Point2 p = new Point2(column, row);

				// Update RGB...
				Color cRgb = new Color(image.getRGB(column, row));
				rgb[column][row] = cRgb;
				// ...and HSB vals
				float[] cHsb = hsb[column][row];
				Color.RGBtoHSB(cRgb.getRed(), cRgb.getBlue(), cRgb.getGreen(),
						cHsb);
	
				//Scale the values of the pitch
				float br = hsb[column][row][2];
				br = (br - minMaxBrigthness[1])/minMaxBrigthness[0];
				image.setRGB(column, row, Color.HSBtoRGB(hsb[column][row][0], hsb[column][row][1], br));

				for (int i=0; i<3; i++) cHsb[i]*=255;

				// Find "Ball" pixels
				if (isBall(cRgb, cHsb)) {
					ballPos = ballPos.add(p);
					ballN++;
					// Makes red pixels orange - for debugging
					// image.setRGB(column, row, Color.ORANGE.getRGB());
				}

				// Find Yellow pixels
				if (isYellow(cRgb, cHsb)) {
					yellowPos = yellowPos.add(p);
					yellowN++;
					yellowPoints.add(new Point2(column, row));
					// Makes yellow pixels orange
					image.setRGB(column, row, Color.ORANGE.getRGB());
				}

				// Find Blue pixels
				if (isBlue(cRgb, cHsb)) {
					bluePos = bluePos.add(p);
					blueN++;
					bluePoints.add(new Point2(column, row));
					// Makes blue pixels bluer
					image.setRGB(column, row, Color.BLUE.getRGB());
				}
			}
		}

		// Get average position of ball
		if (ballN != 0)
			ballPos = ballPos.div(ballN);

		// Get average position of yellow bot
		if (yellowN != 0)
			yellowPos = yellowPos.div(yellowN);
		ArrayList<Point2> newYellow = Point2.removeOutliers(yellowPoints,
				yellowPos);
		yellowPos.filterPoints(newYellow);

		// Get average position of blue bot
		if (blueN > 0)
			bluePos = bluePos.div(blueN);
		ArrayList<Point2> newBlue = Point2.removeOutliers(bluePoints, bluePos);
		bluePos.filterPoints(newBlue);

		// Calculates where ball is going
		Point2 avgPrevPos = new Point2(0, 0);
		for (Point2 p : prevFramePos)
			avgPrevPos = avgPrevPos.add(p);
		avgPrevPos = avgPrevPos.div(prevFramePos.length);
		avgPrevPos = avgPrevPos.subtract(ballPos).mult(-5).add(ballPos);

		// TODO: fix orientation code
		double yellowOrientation = 0;

		// Point2 blackPos = new Point2();
		Point2 blackPos = findBlackDot(yellowPos);
		blackPos = blackPos.subtract(yellowPos).mult(-5).add(yellowPos);

		// Update World State
		state.setBallPosition(ballPos);
		state.setRobotPosition(0, 0, yellowPos);
		state.setRobotFacing(0, 0, yellowOrientation);

		// Create graphical representation
		Graphics imageGraphics = image.getGraphics();
		Graphics frameGraphics = label.getGraphics();

		// Ball location (and direction)
		if (pointInPitch(ballPos)) {
			imageGraphics.setColor(Color.red);
			imageGraphics.drawLine(0, ballPos.getY(), 640, ballPos.getY());
			imageGraphics.drawLine(ballPos.getX(), 0, ballPos.getX(), 480);
			if (lineSize(ballPos, avgPrevPos) > VECTOR_THRESHOLD) {
				imageGraphics.drawLine(ballPos.getX(), ballPos.getY(),
						avgPrevPos.getX(), avgPrevPos.getY());
			}
		}

		// Yellow robots locations
		if (pointInPitch(yellowPos)) {
			imageGraphics.setColor(Color.yellow);
			imageGraphics.drawOval(yellowPos.getX() - 15,
					yellowPos.getY() - 15, 30, 30);
		}

		// draw orientation (temp)
		if (pointInPitch(yellowPos)) {
			imageGraphics.drawLine(yellowPos.getX(), yellowPos.getY(),
					blackPos.getX(), blackPos.getY());
		}

		// Blue robots locations
		if (pointInPitch(bluePos)) {
			imageGraphics.setColor(Color.blue);
			imageGraphics.drawOval(bluePos.getX() - 15, bluePos.getY() - 15,
					30, 30);
		}

		// Saves this frame's ball position and shifts previous frames'
		// positions
		for (int i = prevFramePos.length - 1; i > 0; i--) {
			prevFramePos[i] = prevFramePos[i - 1];
		}
		prevFramePos[0] = ballPos;

		/* Display the FPS that the vision system is running at. */
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

	private static final int playerRadius = 18; // TODO: find a good val

	/**
	 * Gets the position of the black dot around some point Used to determine a
	 * robot's orientation, given its centre point
	 * 
	 * @param colorCenter
	 *            the center of the robot's yellow/blue
	 */
	private Point2 findBlackDot(Point2 colorCenter) {
		// bounding rect to search for black pixels
		int xs = Math.max(0, colorCenter.getX() - playerRadius);
		int ys = Math.max(0, colorCenter.getY() - playerRadius);
		int xe = Math.min(WIDTH, colorCenter.getX() + playerRadius);
		int ye = Math.min(HEIGHT, colorCenter.getY() + playerRadius);

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

				// add if "black"
				if (isBlack(cRgb, cHsb))
					pts.add(new Point2(ix, iy));
			}

		// do k-means
		Cluster c = Kmeans.doKmeans(pts, new Point2(colorCenter))[0]; // only 1
																		// cluster

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
	private boolean isBlack(Color rgb, float[] hsb) {
		return hsb[1] < 0.3f && hsb[2] < 0.3f;
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
		return (c.getRed() > 125 && c.getGreen() < 40 
				&& c.getBlue() < 30 && hsb[1] > 140);
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
		return (c.getRed() > 130 &&
				c.getGreen() > 70 && c.getGreen() < 120 && c
				.getBlue() < 40 && 
				hsb[0]>200 && hsb[1]>170 && hsb[2]>100);
		//Hue > 200 ; Sat > 170 ; Value > 100
	}
	
	/**
	 * Finds the brightest and darkest points on the image
	 * 
	 * @param image
	 *            The Image to be used for min max brightness foundation
	 * 
	 * @return The list of two float HSB values representing the brightest and the darkest values
	 */
	private float[] findMinMaxBrigthness(BufferedImage image) {
		float maxBr = 0;
		float minBr = 1;
		
		for (int row = 0; row < image.getHeight(); row++) {
			for (int column = 0; column < image.getWidth(); column++) {
				Color change = new Color(image.getRGB(column, row));
				float[] cHsb = hsb[column][row];
				Color.RGBtoHSB(change.getRed(), change.getBlue(), change.getGreen(),
						cHsb);
				float br = hsb[column][row][2];
				if (br >= maxBr) {
					maxBr = br;
				}
					
				if (br <= minBr) {
					minBr = br;
				}
			}	
		}
		float[] values = {maxBr, minBr};
		return values;
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
		return (c.getRed() > 0 && c.getRed() < 50 
				&& c.getGreen() > 50 && c.getGreen() < 100
				&& c.getBlue() > 70 && c.getBlue() < 150
				&& 110<hsb[0] && hsb[0]<140
				&& hsb[1]>120
				&& 70<hsb[2] && hsb[2]<100);
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
	/*public double findOrientation(ArrayList<Integer> xpoints,
			ArrayList<Integer> ypoints, int meanX, int meanY,
			BufferedImage image, boolean showImage) {
		double angle;
		double goodAngle = 0;
		int goodAngleCount = 0;

		for (angle = 0; angle < 360; angle++) {
			int newCentX = meanX
					+ (int) Math.round(4 * Math.cos(angle * 2 * Math.PI / 360));
			int newCentY = meanY
					+ (int) Math.round(4 * Math.sin(angle * 2 * Math.PI / 360));
			int yellowCountBack = 0;
			int yellowCountSides = 0;
			int yellowCountFront = 0;

			// Goes to back of bot
			for (int i = 0; i < 20; i++) {
				int x = newCentX
						+ (int) Math.round(i
								* Math.cos(angle * 2 * Math.PI / 360));
				int y = newCentY
						+ (int) Math.round(i
								* Math.sin(angle * 2 * Math.PI / 360));
				Color c = new Color(image.getRGB(x, y));
				if (isYellow(c)) {
					yellowCountBack++;
				}
			}
			// Goes to sides of bot
			for (int i = -20; i < 20; i++) {
				int x = newCentX
						+ (int) Math.round(i
								* Math.cos((angle + 90) * 2 * Math.PI / 360));
				int y = newCentY
						+ (int) Math.round(i
								* Math.sin((angle + 90) * 2 * Math.PI / 360));
				Color c = new Color(image.getRGB(x, y));
				if (isYellow(c)) {
					yellowCountSides++;
				}
			}

			// Goes to front of bot
			for (int i = 0; i < 25; i++) {
				int x = newCentX
						+ (int) Math.round(i
								* Math.cos((angle + 180) * 2 * Math.PI / 360));
				int y = newCentY
						+ (int) Math.round(i
								* Math.sin((angle + 180) * 2 * Math.PI / 360));
				Color c = new Color(image.getRGB(x, y));
				if (isYellow(c)) {
					yellowCountFront++;
				}
			}

			// Checks if angle is good
			// (note the thresholds change constantly during a 24-hour cycle due
			// to lighting)
			if (yellowCountBack >= 2 && yellowCountBack <= 4
					&& yellowCountSides >= 20 && yellowCountFront >= 18) {
				goodAngle += angle;
				goodAngleCount++;
				// System.out.println(yellowCountBack + " " + yellowCountSides +
				// " " + yellowCountFront);
			}

		}

		if (goodAngleCount != 0) {
			goodAngle /= goodAngleCount;
			goodAngle += 180;
			if (goodAngle > 360)
				goodAngle -= 360;
			goodAngle = 360 - goodAngle;
		} else
			goodAngle = prevBestAngle;

		int x = meanX
				+ (int) Math
						.round(10 * Math.cos(goodAngle * 2 * Math.PI / 360));
		int y = meanY
				+ (int) Math.round(-10
						* Math.sin(goodAngle * 2 * Math.PI / 360));
		image.getGraphics().setColor(Color.black);
		image.getGraphics().drawOval(x - 2, y - 2, 4, 4);

		prevBestAngle = goodAngle;
		return goodAngle;
	}*/

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