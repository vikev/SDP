package sdp.pc.vision;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
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
 * from group 1 of SDP 2013.
 * 
 * For programmers: the bulk of the work is done by the method 'processImage'.
 * Don't touch stuff like initGui unless need be.
 * 
 * @author Group 8, SDP 2014
 * 
 */
public class Vision extends WindowAdapter implements CaptureCallback {

	// Vision-specific paramters
	private static final int WIDTH = 640, HEIGHT = 480,
			VIDEO_STANDARD = V4L4JConstants.STANDARD_PAL, CHANNEL = 0,
			PLAYER_RADIUS = 18, FRAME_IGNORE_COUNT = 50,
			ANGLE_SMOOTHING_FRAME_COUNT = 4, MIN_POINTS_BOT = 10;
	private static final String DEVICE = "/dev/video0";
	private static final double VECTOR_THRESHOLD = 3.0;

	private static Color[][] rgb = new Color[700][520];
	private static double[][] angleSmoothing = new double[4][ANGLE_SMOOTHING_FRAME_COUNT];
	private static float[][][] hsb = new float[700][520][3];
	private static float[] cHsb = new float[3];
	private static int[] pointsCount = new int[4];
	private static int angSmoothingWriteIndex = 0, stateCounter = 0;;
	private static Point2 circlePt = new Point2(-1, -1);
	private static PitchConstants pitchConsts = new PitchConstants(0);
	private static ThresholdsState thresh = new ThresholdsState();
	private static JLabel frameLabel;
	private static VideoDevice videoDevice;
	private static FrameGrabber frameGrabber;

	// Used for normalisation (first float is max brightness, second is min
	// brightness)
	private static float[] minMaxBrightness = { 1.0f, 0.0f };

	// Used to denote if video feed has been pre-processed; using integer
	// so we can count frames and ignore the first few
	private static int keyframe = 0;
	
	// For FPS calculation
	private static long initialTime; 

	public static Point2[][] points = new Point2[1000][1000];
	public static float fps;
	public static ArrayList<Point2> pitchPoints = new ArrayList<Point2>();
	public static WorldState state = new WorldState();
	public static Point2 requestedData = new Point2(-1, -1);
	public static JFrame frame;
	public static boolean hullCalculated = false;
	public static boolean edgesCalibrated;

	// Added in to help split up into sections
	public static Point2 leftTop = new Point2(0, 0);
	public static Point2 rightBottom = new Point2(0, 0);
	public static boolean sectionsDone = false;

	// Used for calculating direction the ball is heading
	public static Point2[] prevFramePos = new Point2[10];

	/**
	 * Provides Java application support. On launch, runs a JFrame window which
	 * displays the video feed with our overlaid data.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
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
		// Initialise prevFramePos to avoid null pointer exception:
		for (int i = 0; i < 10; i++)
			prevFramePos[i] = new Point2(0, 0);

		Vision.state = state;
		pitchConsts.loadConstants("pitch0");
		pitchConsts.uploadConstants(thresh);

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

				// Ensure the pre-process is only ran once during
				// initialisation; but ignore first few frames (which are always
				// highly distorted)
				processImage(frameImage);

				if (!hullCalculated) {
					keyframe++;
					if (keyframe >= FRAME_IGNORE_COUNT && edgesCalibrated) {
						hullCalculated = true;
						System.out
								.println("Calculating convex hull with absolute boundaries "
										+ leftTop.toString()
										+ "; "
										+ rightBottom.toString());
						preprocess(frameImage);

					} else if (stateCounter == 0 && keyframe < FRAME_IGNORE_COUNT) {
						System.out
								.println("Waiting a a few frames before processing convex hull...");
						stateCounter++;
					}
				}
			}
		});

		frameGrabber.startCapture();

		// Mouse Listener
		frame.addMouseListener(new Calibration());
	}

	/**
	 * Expensive method used to build a convex hull around white points (our
	 * table border due to the white tape). Also, calculates the brightest and
	 * least brightest points to be used for brightness normalisation.
	 * 
	 * Only needs to occur once during initialisation; stores the brightness
	 * values in minMaxBrightness, and the points that belong to the pitch in
	 * pitchPoints.
	 * 
	 * @param image
	 *            - image on which to base the pre-processing
	 */
	private static void preprocess(BufferedImage image) {
		keyframe++;

		ArrayList<Point2> whitePoints = new ArrayList<Point2>();
		pitchPoints = new ArrayList<Point2>();

		// Find borders
		for (int x = 0; x < 1000; x++) {
			for (int y = 0; y < 1000; y++) {
				points[x][y] = new Point2(-1, -1);
			}
		}
		for (int row = leftTop.getY(); row < rightBottom.getY(); row++) {
			for (int column = leftTop.getX(); column < rightBottom.getX(); column++) {

				// Color pixelColorRGB = new Color(image.getRGB(column, row));
				Color pixelColorRGB = normaliseColor(image, row, column);
				Point2 p = new Point2(column, row);
				if (isWhite(pixelColorRGB, cHsb) && row < 470) {
					whitePoints.add(p);
				}
			}
		}

		LinkedList<Point2> borders = Alg.convexHull(whitePoints);
		for (int row = leftTop.getY(); row < rightBottom.getY(); row++) {
			for (int column = leftTop.getX(); column < rightBottom.getX(); column++) {
				Point2 p = new Point2(column, row);

				if (Alg.isInHull(borders, p))
					pitchPoints.add(p);
				points[column][row] = new Point2(column, row);
			}
		}

		minMaxBrightness = findMinMaxBrigthness(image);
	}

	/**
	 * Identifies objects of interest (ball, robots) in the image while in play.
	 * The information from it (positions, orientations, predictions) will be
	 * passed to a WorldState object (called state) to be used by other files.
	 * 
	 * I've analysed the performance of running this script using flat
	 * millisecond durations. Specifically:
	 * 
	 * <ul>
	 * <li>Initialise colour recognition values: 0ms/frame</li>
	 * <li>Recognise colour regions: 37ms/frame (36 for normalising only, 1 for
	 * everything else (!)</li>
	 * <li>Get average positions: 0ms/frame</li>
	 * <li>Calculate ball direction: 0ms/frame</li>
	 * <li>Update world state: 0ms/frame</li>
	 * <li>Create graphical representations: 0ms/frame</li>
	 * <li>Draw ball location and direction: 0ms/frame</li>
	 * <li>Shift ball frames: 0ms/frame</li>
	 * <li>Draw FPS, mouse-position, etc text: 0ms/frame</li>
	 * <li>Draw final image to screen: 1ms/frame</li>
	 * </ul>
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

		// Loop through all table values, recognising pixel regions as necessary
		for (Point2 p : pitchPoints) {
			int column = p.getX();
			int row = p.getY();

			// Color pixelColorRGB = new Color(image.getRGB(column, row));

			Color pixelColorRGB = normaliseColor(image, row, column);

			// Find "Ball" pixels
			if (isBall(pixelColorRGB, cHsb)) {
				ballPos = ballPos.add(p);
				ballCounter++;
				// Makes red pixels more red - for debugging
				image.setRGB(column, row, Color.RED.getRGB());
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

		if (sectionsDone == true) {
			// imageGraphics.drawLine(x1, y1, x2, y2);
			Graphics imageGraphics = image.getGraphics();
			// Graphics frameGraphics = label.getGraphics();
			imageGraphics.setColor(Color.white);
			// imageGraphics.drawLine(leftTop.getX(), leftTop.getY(),
			// leftBottom.getX(), leftBottom.getY());
			// imageGraphics.drawLine(rightTop.getX(), rightTop.getY(),
			// rightBottom.getX(), rightBottom.getY());

		}

		// Get average position of ball
		if (ballCounter > 0)
			ballPos = ballPos.div(ballCounter);

		// Get average position of left yellow bot
		if (yellowLeftCounter > 0) {
			yellowLeftPos = yellowLeftPos.div(yellowLeftCounter);
			ArrayList<Point2> newYellow = Point2.removeOutliers(
					yellowLeftPoints, yellowLeftPos);
			pointsCount[Constants.ROBOT_YELLOW_LEFT] = newYellow.size();
			yellowLeftPos.filterPoints(newYellow);
		}

		// Get average position of left blue bot
		if (blueLeftCounter > 0) {
			blueLeftPos = blueLeftPos.div(blueLeftCounter);
			ArrayList<Point2> newBlue = Point2.removeOutliers(blueLeftPoints,
					blueLeftPos);
			pointsCount[Constants.ROBOT_BLUE_LEFT] = newBlue.size();
			blueLeftPos.filterPoints(newBlue);
		}

		// Get average position of right yellow bot
		if (yellowRightCounter > 0) {
			yellowRightPos = yellowRightPos.div(yellowRightCounter);
			ArrayList<Point2> newYellow2 = Point2.removeOutliers(
					yellowRightPoints, yellowRightPos);
			pointsCount[Constants.ROBOT_YELLOW_RIGHT] = newYellow2.size();
			yellowRightPos.filterPoints(newYellow2);
		}

		// Get average position of right blue bot
		if (blueRightCounter > 0) {
			blueRightPos = blueRightPos.div(blueRightCounter);
			ArrayList<Point2> newBlue2 = Point2.removeOutliers(blueRightPoints,
					blueRightPos);
			pointsCount[Constants.ROBOT_BLUE_RIGHT] = newBlue2.size();
			blueRightPos.filterPoints(newBlue2);
		}

		// Calculates where ball is going
		Point2 avgPrevPos = new Point2(0, 0);
		for (Point2 p : prevFramePos)
			avgPrevPos = avgPrevPos.add(p);
		avgPrevPos = avgPrevPos.div(prevFramePos.length);
		avgPrevPos = avgPrevPos.subtract(ballPos).mult(-5).add(ballPos);

		// Create graphical representation
		Graphics imageGraphics = image.getGraphics();
		Graphics frameGraphics = frameLabel.getGraphics();

		// TODO: fix orientation code
		double yellowOrientation = findOrientation(image, yellowLeftPos,
				imageGraphics, Constants.ROBOT_YELLOW_LEFT);

		// Point2 blackPos = new Point2();
		// Point2 blackPos = findBlackDot(image, yellowLeftPos);
		// blackPos =
		// blackPos.subtract(yellowLeftPos).mult(-5).add(yellowLeftPos);

		// Update World State
		state.setBallPosition(ballPos);
		state.setBallFacing(Math.atan2(ballPos.getY() - avgPrevPos.getY(),
				ballPos.getX() - avgPrevPos.getX()) * 180 / Math.PI);
		state.setBallVelocity(new Point2(ballPos.getX() - avgPrevPos.getX(),
				ballPos.getY() - avgPrevPos.getY()));
		int ballPosDeltaY = ballPos.getY() - avgPrevPos.getY();
		int ballPosDeltaX = ballPos.getX() - avgPrevPos.getX();
		if (ballPosDeltaY == 0 & ballPosDeltaX == 0) {
			state.setBallFacing(-1);
		} else {
			state.setBallFacing(Math.atan2(ballPos.getY() - avgPrevPos.getY(),
					ballPos.getX() - avgPrevPos.getX()) * 180 / Math.PI);
		}
		state.setRobotPosition(0, 0, yellowLeftPos);
		state.setRobotFacing(0, 0, yellowOrientation);

		// Ball location (and direction)
		if (Alg.pointInPitch(ballPos)) {
			imageGraphics.setColor(Color.red);
			imageGraphics.drawLine(0, ballPos.getY(), 640, ballPos.getY());
			imageGraphics.drawLine(ballPos.getX(), 0, ballPos.getX(), 480);
			drawCircle(FutureBall.estimateRealStopPoint(), imageGraphics,
					Constants.GRAY_BLEND, Constants.ROBOT_HEAD_RADIUS);
			if (FutureBall.collision.getX() > 0) {
				drawCircle(FutureBall.collision, imageGraphics,
						Constants.GRAY_BLEND, 5);
			}
			if (Alg.lineSize(ballPos, avgPrevPos) > VECTOR_THRESHOLD) {
				imageGraphics.drawLine(ballPos.getX(), ballPos.getY(),
						avgPrevPos.getX(), avgPrevPos.getY());
			}
		}

		// Yellow robots locations
		if (Alg.pointInPitch(yellowLeftPos)
				&& pointsCount[Constants.ROBOT_YELLOW_LEFT] >= MIN_POINTS_BOT) {
			drawCircle(yellowLeftPos, imageGraphics, Constants.YELLOW_BLEND,
					Constants.ROBOT_CIRCLE_RADIUS);
			// findOrientation(image, yellowLeftPos, imageGraphics,
			// Constants.ROBOT_YELLOW_LEFT);
			findOrientationByDot(yellowLeftPos.getX(), yellowLeftPos.getY(),
					image, true);
		}

		if (Alg.pointInPitch(yellowRightPos)
				&& pointsCount[Constants.ROBOT_YELLOW_RIGHT] >= MIN_POINTS_BOT) {
			drawCircle(yellowRightPos, imageGraphics, Constants.YELLOW_BLEND,

			Constants.ROBOT_CIRCLE_RADIUS);
			// findOrientation(image, yellowRightPos, imageGraphics,
			// Constants.ROBOT_YELLOW_RIGHT);
			findOrientationByDot(yellowRightPos.getX(), yellowRightPos.getY(),
					image, true);
		}

		// draw orientation (temp)
		// if (Alg.pointInPitch(yellowLeftPos)) {
		// imageGraphics.drawLine(yellowLeftPos.getX(), yellowLeftPos.getY(),
		// blackPos.getX(), blackPos.getY());
		// }

		// TODO: Implement blackpos for all bots

		// Blue robots locations
		if (Alg.pointInPitch(blueLeftPos)
				&& pointsCount[Constants.ROBOT_BLUE_LEFT] >= MIN_POINTS_BOT) {
			drawCircle(blueLeftPos, imageGraphics, Constants.BLUE_BLEND,
					Constants.ROBOT_CIRCLE_RADIUS);
			// findOrientation(image, blueLeftPos, imageGraphics,
			// Constants.ROBOT_BLUE_LEFT);
			findOrientationByDot(blueLeftPos.getX(), blueLeftPos.getY(), image,
					true);
		}

		if (Alg.pointInPitch(blueRightPos)
				&& pointsCount[Constants.ROBOT_BLUE_RIGHT] >= MIN_POINTS_BOT) {
			drawCircle(blueRightPos, imageGraphics, Constants.BLUE_BLEND,
					Constants.ROBOT_CIRCLE_RADIUS);
			// findOrientation(image, blueRightPos, imageGraphics,
			// Constants.ROBOT_BLUE_RIGHT);
			findOrientationByDot(blueRightPos.getX(), blueRightPos.getY(),
					image, true);
		}

		if (Alg.pointInPitch(circlePt)) {
			drawCircle(circlePt, imageGraphics, Constants.GRAY_BLEND,
					Constants.ROBOT_HEAD_RADIUS);
		}

		angSmoothingWriteIndex++;
		angSmoothingWriteIndex %= ANGLE_SMOOTHING_FRAME_COUNT;

		// Draw centre line
		// imageGraphics.setColor(new Color(1.0f, 1.0f, 1.0f, 0.3f));
		// imageGraphics.drawLine(Constants.TABLE_CENTRE_X,
		// Constants.TABLE_MIN_Y + 1, Constants.TABLE_CENTRE_X,
		// Constants.TABLE_MAX_Y - 1);

		Point2 dPt = new Point2(requestedData.getX(), requestedData.getY());
		if (Alg.pointInPitch(dPt)) {
			circlePt = requestedData.copy();
			Color s = rgb[requestedData.getX()][requestedData.getY()];
			if (s != null) {
				System.out.println();
				System.out.println("Point data at " + requestedData.toString()
						+ ":");
				System.out.println("RGB: " + s.getRed() + " " + s.getGreen()
						+ " " + s.getBlue());
				float[] h = hsb[requestedData.getX()][requestedData.getY()];
				System.out.println("HSB: " + h[0] + " " + h[1] + " " + h[2]);
			}

			requestedData = new Point2(-1, -1);
		}

		// Saves this frame's ball position and shifts previous frames'
		// positions
		for (int i = prevFramePos.length - 1; i > 0; i--) {
			prevFramePos[i] = prevFramePos[i - 1];
		}
		prevFramePos[0] = ballPos;

		// Display the FPS that the vision system is running at
		long after = System.currentTimeMillis(); // Used to calculate the FPS.
		fps = (1.0f) / ((after - initialTime) / 1000.0f);
		imageGraphics.setColor(Color.white);
		imageGraphics.drawString("FPS: " + (int) fps, 15, 15);
		int x = 1, y = 0;
		java.awt.Point pos;

		// Draw mouse position, RGB, and HSB values to screen
		pos = frame.getMousePosition();
		if (pos != null) {
			x = (int) Math.round(pos.getX()) - Constants.X_FRAME_OFFSET;
			y = (int) Math.round(pos.getY()) - Constants.Y_FRAME_OFFSET;
			if (Alg.pointInPitch(new Point2(x, y))) {

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

	/**
	 * Given a yellow/blue robot centroid, finds the darkest region nearby
	 * (cheaply), circles it, and draws a line through the facing angle,
	 * returning the facing angle in degrees.
	 * 
	 * Author s1143704
	 */

	private void findOrientationByDot(int meanX, int meanY,
			BufferedImage image, boolean showImage) {

		// Find the smallest polygon that covers the green plate so as to only
		// look within it (turns out the method is pretty fast)
		ArrayList<Point2> greenPoints = new ArrayList<Point2>();
		for (int i = -25; i < 25; i++)
			for (int j = -25; j < 25; j++) {
				int column = meanX + i;
				int row = meanY + j;
				if (Alg.pointInPitch(new Point2(column, row))) {
					Color pixelColorRGB = new Color(image.getRGB(column, row));
					cHsb = hsb[column][row];
					if (isGreen(pixelColorRGB, cHsb)) {
						image.setRGB(column, row, Color.GREEN.getRGB());
						greenPoints.add(new Point2(column, row));
					}
				}
			}
		LinkedList<Point2> hull = new LinkedList<Point2>();
		if (!greenPoints.isEmpty()) {
			hull = Alg.convexHull(greenPoints);
		}
		int pxC = 0;
		int pxX = 0, pxY = 0;
		for (int i = -25; i < 25; i++)
			for (int j = -25; j < 25; j++) {
				int column = meanX + i;
				int row = meanY + j;

				if (Alg.isInHull(hull, new Point2(column, row))) {
					Color pixelColorRGB = new Color(image.getRGB(column, row));
					if (isBlack(pixelColorRGB, hsb[column][row])) {
						pxC++;
						pxX += column;
						pxY += row;
					}
				}

			}

		if (pxC != 0) {
			pxX /= pxC;
			pxY /= pxC;
		}

		Point2 pt = new Point2(pxX, pxY);
		drawCircle(pt, image.getGraphics(), Constants.GRAY_BLEND,
				Constants.ROBOT_HEAD_RADIUS);
		if (Alg.pointInPitch(new Point2(pxX, pxY))) {
			image.getGraphics().drawLine(meanX, meanY, pxX, pxY);
		}
	}

	private double findOrientation(BufferedImage image, Point2 centroid,
			Graphics gfx, int iden) {

		// Point here is to find the smallest polygon that covers the green
		// plate so as to only look within it (turns out the method is pretty
		// fast)
		ArrayList<Point2> greenPoints = new ArrayList<Point2>();
		for (int i = -25; i < 25; i++)
			for (int j = -25; j < 25; j++) {
				int column = centroid.getX() + i;
				int row = centroid.getY() + j;
				if (Alg.pointInPitch(new Point2(column, row))) {
					Color pixelColorRGB = new Color(image.getRGB(column, row));
					cHsb = hsb[column][row];
					if (isGreen(pixelColorRGB, cHsb)) {
						image.setRGB(column, row, Color.GREEN.getRGB());
						greenPoints.add(new Point2(column, row));
					}
				}
			}
		LinkedList<Point2> hull = new LinkedList<Point2>();
		if (!greenPoints.isEmpty()) {

			// Contains border of green plate
			hull = Alg.convexHull(greenPoints);
		}

		// Get best angle
		double angBest = 0.0;
		if (Alg.pointInPitch(centroid)) {
			double r = 0;
			double brBest = 1.0;
			for (int i = 0; i < Constants.HEAD_ARC_FIDELITY; i++) {
				r += 2.0 * Math.PI / Constants.HEAD_ARC_FIDELITY;
				int row = centroid.getY()
						+ (int) (Constants.HEAD_ENUM_RADIUS * Math.sin(r));
				int column = centroid.getX()
						+ (int) (Constants.HEAD_ENUM_RADIUS * Math.cos(r));
				if (Alg.pointInPitch(new Point2(column, row))) {
					// TODO: implement next if-clause better
					if (hsb[column][row][2] < brBest
							&& Alg.isInHull(hull, new Point2(column, row))) {
						brBest = hsb[column][row][2];
						angBest = r;
					}
				}
			}

			// Smooth angle
			angleSmoothing[iden][angSmoothingWriteIndex] = angBest;
			angBest = 0;
			for (int j = 0; j < ANGLE_SMOOTHING_FRAME_COUNT; j++) {
				angBest += angleSmoothing[iden][j];
			}
			angBest /= ANGLE_SMOOTHING_FRAME_COUNT;

			/*
			 * // Draw to screen Point2 pt = new Point2(centroid.getX() + (int)
			 * (Constants.HEAD_ENUM_RADIUS * Math.cos(angBest)), centroid.getY()
			 * + (int) (Constants.HEAD_ENUM_RADIUS * Math .sin(angBest)));
			 * drawCircle(pt, gfx, Constants.GRAY_BLEND,
			 * Constants.ROBOT_HEAD_RADIUS); gfx.drawLine(centroid.getX(),
			 * centroid.getY(), (int) (centroid.getX() -
			 * Constants.HEAD_ENUM_RADIUS * 2.0 Math.cos(angBest)), (int)
			 * (centroid.getY() - Constants.HEAD_ENUM_RADIUS * 2.0
			 * Math.sin(angBest)));
			 */
		}
		if (angBest > Math.PI)
			return (angBest - Math.PI) * 180.0 / Math.PI;
		return (angBest + Math.PI) * 180.0 / Math.PI;
	}

	/**
	 * Abstraction of Graphics.drawOval which simplifies our circle drawing
	 */
	private void drawCircle(Point2 centrePt, Graphics gfx, Color c, int radius) {
		gfx.setColor(c);
		gfx.drawOval(centrePt.getX() - radius, centrePt.getY() - radius,
				radius * 2, radius * 2);
	}

	/**
	 * Finds the brightest and darkest points on the image. Used for brightness
	 * normalisation (in normaliseColor).
	 * 
	 * @param image
	 *            The Image to be used for min max brightness foundation
	 * 
	 * @return The list of two float HSB values representing the brightest and
	 *         the darkest values (format {max, min})
	 */
	private static float[] findMinMaxBrigthness(BufferedImage image) {
		float maxBr = 0.0f;
		float minBr = 1.0f;

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
	 * Uses the HSB colour space to normalise all colours in our desired region
	 * by brightness, i.e. used to scale the brightness of every pixel on the
	 * pitch so that the whole range of brightnesses is from 0.0 to 1.0.
	 * 
	 * We've analysed the performance of this method as well. Below are some
	 * timestamps in nanoseconds, denoted using %% in a comment
	 */
	private static Color normaliseColor(BufferedImage image, int row, int column) {
		// Normalise colour values:
		// Update RGB handle

		// %% 769 nanoseconds:
		Color pixelColorRGB = new Color(image.getRGB(column, row));
		rgb[column][row] = pixelColorRGB;
		// %%

		// Update HSB handle
		// %% 1118 nanoseconds:
		cHsb = hsb[column][row];
		Color.RGBtoHSB(pixelColorRGB.getRed(), pixelColorRGB.getGreen(),
				pixelColorRGB.getBlue(), cHsb);
		// %%

		// Scale the values of the pitch
		// %% 2584 nanoseconds:
		float br = hsb[column][row][2];

		// hsb is of the form {max, min}
		br = (br - minMaxBrightness[1]) / minMaxBrightness[0];
		if (br < 0)
			br = 0;
		if (br > 1)
			br = 1;
		image.setRGB(column, row,
				Color.HSBtoRGB(hsb[column][row][0], hsb[column][row][1], br));
		// %%

		// %% 699 nanoseconds:
		hsb[column][row] = cHsb;
		// %%
		return pixelColorRGB;
	}

	/**
	 * Gets the position of the black dot around some point Used to determine a
	 * robot's orientation, given its centre point
	 * 
	 * @param colorCenter
	 *            the centre of the robot's yellow/blue
	 */
	@SuppressWarnings("unused")
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

		// do k-means (one cluster only)
		Cluster c = Kmeans.doKmeans(pts, new Point2(colorCenter))[0];

		if (pts.size() == 0)
			return new Point2(colorCenter);

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
	private boolean isBlack(Color c, float[] hsb) {
		return hsb[0] <= thresh.getGrey_h_high()
				&& hsb[0] >= thresh.getGrey_h_low()
				&& hsb[1] <= thresh.getGrey_s_high()
				&& hsb[1] >= thresh.getGrey_s_low()
				&& hsb[2] <= thresh.getGrey_v_high()
				&& hsb[2] >= thresh.getGrey_v_low()
				&& c.getRed() <= thresh.getGrey_r_high()
				&& c.getRed() >= thresh.getGrey_r_low()
				&& c.getGreen() <= thresh.getGrey_g_high()
				&& c.getGreen() >= thresh.getGrey_g_low()
				&& c.getBlue() <= thresh.getGrey_b_high()
				&& c.getBlue() >= thresh.getGrey_b_low();
	}

	/**
	 * Determines if a pixel is from the green plates that hold the yellow and
	 * blue i's. Used to calculate the polygons that determine the plates in
	 * order to look for the black dot within them.
	 * 
	 * @param rgb
	 *            The RGB colours for the pixel.
	 * @param hsb
	 *            The HSV values for the pixel.
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the green plates), false
	 *         otherwise.
	 */
	private boolean isGreen(Color c, float[] hsb) {
		return hsb[0] <= thresh.getGreen_h_high()
				&& hsb[0] >= thresh.getGreen_h_low()
				&& hsb[1] <= thresh.getGreen_s_high()
				&& hsb[1] >= thresh.getGreen_s_low()
				&& hsb[2] <= thresh.getGreen_v_high()
				&& hsb[2] >= thresh.getGreen_v_low()
				&& c.getRed() <= thresh.getGreen_r_high()
				&& c.getRed() >= thresh.getGreen_r_low()
				&& c.getGreen() <= thresh.getGreen_g_high()
				&& c.getGreen() >= thresh.getGreen_g_low()
				&& c.getBlue() <= thresh.getGreen_b_high()
				&& c.getBlue() >= thresh.getGreen_b_low();
	}

	/**
	 * Determines if a pixel is from the white tape on the pitch. Used to
	 * calculate the smallest possible polygon that captures the entire pitch.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsb
	 *            The HSV values for the pixel.
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of white tape), false otherwise.
	 */
	private static boolean isWhite(Color c, float[] hsb) {
		boolean h = Alg.withinBounds(hsb[0], 0.05f, 0.05f);
		boolean b = Alg.withinBounds(hsb[0], 0.2f, 0.2f);
		boolean rgb = c.getRed() > 90 && c.getRed() < 130 && c.getGreen() > 70
				&& c.getGreen() < 130;
		return rgb && h && b;
	}

	/**
	 * Determines if a pixel is part of the ball, based on input RGB colours and
	 * hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsb
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the ball), false otherwise.
	 */
	private boolean isBall(Color c, float[] hsb) {
		return hsb[0] <= thresh.getBall_h_high()
				&& hsb[0] >= thresh.getBall_h_low()
				&& hsb[1] <= thresh.getBall_s_high()
				&& hsb[1] >= thresh.getBall_s_low()
				&& hsb[2] <= thresh.getBall_v_high()
				&& hsb[2] >= thresh.getBall_v_low()
				&& c.getRed() <= thresh.getBall_r_high()
				&& c.getRed() >= thresh.getBall_r_low()
				&& c.getGreen() <= thresh.getBall_g_high()
				&& c.getGreen() >= thresh.getBall_g_low()
				&& c.getBlue() <= thresh.getBall_b_high()
				&& c.getBlue() >= thresh.getBall_b_low();
	}

	/**
	 * Determines if a pixel is part of the yellow T, based on input RGB colours
	 * and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsb
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the yellow T), false otherwise.
	 */
	private boolean isYellow(Color c, float[] hsb) {
		return hsb[0] <= thresh.getYellow_h_high()
				&& hsb[0] >= thresh.getYellow_h_low()
				&& hsb[1] <= thresh.getYellow_s_high()
				&& hsb[1] >= thresh.getYellow_s_low()
				&& hsb[2] <= thresh.getYellow_v_high()
				&& hsb[2] >= thresh.getYellow_v_low()
				&& c.getRed() <= thresh.getYellow_r_high()
				&& c.getRed() >= thresh.getYellow_r_low()
				&& c.getGreen() <= thresh.getYellow_g_high()
				&& c.getGreen() >= thresh.getYellow_g_low()
				&& c.getBlue() <= thresh.getYellow_b_high()
				&& c.getBlue() >= thresh.getYellow_b_low();
	}

	/**
	 * Determines if a pixel is part of the blue T, based on input RGB colours
	 * and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsb
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the blue T), false otherwise.
	 */
	private boolean isBlue(Color c, float[] hsb) {
		return hsb[0] <= thresh.getBlue_h_high()
				&& hsb[0] >= thresh.getBlue_h_low()
				&& hsb[1] <= thresh.getBlue_s_high()
				&& hsb[1] >= thresh.getBlue_s_low()
				&& hsb[2] <= thresh.getBlue_v_high()
				&& hsb[2] >= thresh.getBlue_v_low()
				&& c.getRed() <= thresh.getBlue_r_high()
				&& c.getRed() >= thresh.getBlue_r_low()
				&& c.getGreen() <= thresh.getBlue_g_high()
				&& c.getGreen() >= thresh.getBlue_g_low()
				&& c.getBlue() <= thresh.getBlue_b_high()
				&& c.getBlue() >= thresh.getBlue_b_low();
	}

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
		// System.out.println("Starting capture at " + WIDTH + "x" + HEIGHT);
	}

	/**
	 * Creates the UI components (the JFrame) and initialises them
	 */
	private void initGUI() {
		frame = new JFrame();
		frameLabel = new JLabel();
		JButton button = new JButton("New Parameters");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ControlGUI(thresh, pitchConsts);
			}
		});
		frame.getContentPane().add(frameLabel);
		frame.getContentPane().add(button, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

	/**
	 * Inherited method for V4L exceptions
	 */
	@Override
	public void exceptionReceived(V4L4JException e) {
		e.printStackTrace();
	}

	/**
	 * Inherited method for drawing next frame
	 */
	@Override
	public void nextFrame(VideoFrame frame) {
		frameLabel.getGraphics().drawImage(frame.getBufferedImage(), 0, 0, WIDTH,
				HEIGHT, null);
		frame.recycle();
	}
}