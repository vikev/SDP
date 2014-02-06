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

	// Camera and image parameters
	private static final int WIDTH = 640, HEIGHT = 480,
			VIDEO_STANDARD = V4L4JConstants.STANDARD_PAL, CHANNEL = 0,
			PLAYER_RADIUS = 18,
			ANGLE_SMOOTHING_FRAME_COUNT = 4, MIN_POINTS_BOT = 10;

	private static final String DEVICE = "/dev/video0";
	private static final double VECTOR_THRESHOLD = 3.0;

	private static Color[][] rgb = new Color[700][520];
	private static double[][] angleSmoothing = new double[4][ANGLE_SMOOTHING_FRAME_COUNT];
	private static float[][][] hsb = new float[700][520][3];
	private static float[] cHsb = new float[3];
	private static int angSmoothingWriteIndex = 0;
	private static int[] pointsCount = new int[4];
	private static ArrayList<Point2> pitchPoints = new ArrayList<Point2>();
	private static WorldState state = new WorldState();
	private static WorldStateThread stateUpdater;
	public static Point2 requestedData = new Point2(-1, -1);
	private static Point2 circlePt = new Point2(-1, -1);
	private static PitchConstants pitchConsts = new PitchConstants(0);
	private static ThresholdsState thresh = new ThresholdsState();

	private static JLabel label;
	public static JFrame frame;
	private long initialTime; // For FPS calculation
	private static VideoDevice videoDevice;
	private static FrameGrabber frameGrabber;
	public static boolean edgesCalibrated = false;

	// Used for normalisation (first float is max brightness, second is min
	// brightness)
	private static float[] minMaxBrightness = { 1.0f, 0.0f };

	// Used to denote if video feed has been pre-processed; using integer
	// so we can count frames and ignore the first few
	private static int keyframe = 0;

	// Used for calculating direction the ball is heading
	static Point2[] prevFramePos = new Point2[10];

	// Added in to help split up into sections
	public static Point2 leftTop = new Point2(0, 0);
	public static Point2 leftBottom = new Point2(0, 0);
	public static Point2 rightTop = new Point2(0, 0);
	public static Point2 rightBottom = new Point2(0, 0);
	public static boolean sectionsDone = false;

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
		Vision.stateUpdater = new WorldStateUpdater(100, state);
		stateUpdater.start();
		
		pitchConsts.loadConstants("pitch0");
		pitchConsts.uploadConstants(thresh);
		Colors.setTreshold(thresh);

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
				
				//get the surface graphics
				Graphics g = label.getGraphics();
				
				//draw the original image
				g.drawImage(frameImage, 0, 0, WIDTH, HEIGHT, null);

				//draw world state overlay
				drawWorld(g);
				
			}
		});

		frameGrabber.startCapture();

		// Mouse Listener
		frame.addMouseListener(new Calibration());
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
	private void drawWorld(Graphics g) {

		//ball location and velocity
		Point2 ballPos = state.getBallPosition();
		Point2 ballVelocity = state.getBallVelocity();
	
		if (Alg.pointInPitch(ballPos)) {
			g.setColor(Color.red);
			g.drawLine(0, ballPos.getY(), 640, ballPos.getY());
			g.drawLine(ballPos.getX(), 0, ballPos.getX(), 480);
			
			//draw velocity if large enough
			if (ballVelocity.length() > VECTOR_THRESHOLD) {
				Point2 velocityPos = ballPos.add(ballVelocity);
				g.drawLine(ballPos.getX(), ballPos.getY(),
						velocityPos.getX(), velocityPos.getY());
			}
		}

		// robots locations
		for(int team = 0; team < 2; team++)
			for(int robot = 0; robot < 2; robot++) {
				
				Point2 pos = state.getRobotPosition(team, robot);
				
				if (Alg.pointInPitch(pos)
						&& pointsCount[Constants.ROBOT_YELLOW_LEFT] >= MIN_POINTS_BOT) {	//TODO: Update for all bots
					drawCircle(pos, g, Constants.YELLOW_BLEND,
							Constants.ROBOT_CIRCLE_RADIUS);

					//TODO: Move to logic
//					findOrientationByDot(pos.getX(), pos.getY(),
//							image, true);
				}
			}

		// ???
		if (Alg.pointInPitch(circlePt)) {
			drawCircle(circlePt, g, Constants.GRAY_BLEND,
					Constants.ROBOT_HEAD_RADIUS);
		}

		// ???
		angSmoothingWriteIndex++;
		angSmoothingWriteIndex %= ANGLE_SMOOTHING_FRAME_COUNT;

		// Draw centre line
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.3f));
		g.drawLine(Constants.TABLE_CENTRE_X,
				Constants.TABLE_MIN_Y + 1, Constants.TABLE_CENTRE_X,
				Constants.TABLE_MAX_Y - 1);

		// ???
		Point2 dPt = new Point2(requestedData.getX(), requestedData.getY());
		if (Alg.pointInPitch(dPt)) {
			circlePt = requestedData.copy();
			Color s = rgb[requestedData.getX()][requestedData.getY()];
			System.out.println("RGB: " + s.getRed() + " " + s.getGreen() + " "
					+ s.getBlue());
			float[] h = hsb[requestedData.getX()][requestedData.getY()];
			System.out.println("HSB: " + h[0] + " " + h[1] + " " + h[2]);
			requestedData = new Point2(-1, -1);
		}

		//TODO: remove
		// Saves this frame's ball position and shifts previous frames'
		// positions
		for (int i = prevFramePos.length - 1; i > 0; i--) {
			prevFramePos[i] = prevFramePos[i - 1];
		}
		prevFramePos[0] = ballPos;

		// Display the FPS we run at
		long after = System.currentTimeMillis(); // Used to calculate the FPS.
		float drawFps = (1.0f) / ((after - initialTime) / 1000.0f);
		double worldFps = stateUpdater.getCurrentFps();
		
		g.setColor(Color.white);
		g.drawString("Draw FPS: " + (int) drawFps, 15, 15);
		g.drawString("World FPS: " + (int) worldFps, 15, 30);

		// Draw mouse position, RGB, and HSB values to screen
		java.awt.Point pos = frame.getMousePosition();
		
		if (pos != null) {
			int x = (int) Math.round(pos.getX()) - Constants.X_FRAME_OFFSET;
			int y = (int) Math.round(pos.getY()) - Constants.Y_FRAME_OFFSET;
			
			if (Alg.pointInPitch(new Point2(x, y))) {
				Color c = new Color(stateUpdater.getCurrentImage().getRGB(x, y));
				float[] hsb = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsb);
				
				g.drawString("Mouse pos: x:" + x + " y:" + y, 15,
						30);

				g.drawString(
						"Color: R:" + c.getRed() + " G:" + c.getGreen() + " B:"
								+ c.getBlue(), 15, 45);
				g.drawString(
						"HSB: H:" + new DecimalFormat("#.###").format(hsb[0])
								+ " S:"
								+ new DecimalFormat("#.###").format(hsb[1])
								+ " B:"
								+ new DecimalFormat("#.###").format(hsb[2]),
						15, 60);
			}
		}

		// Finally draw the image to screen
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
		JButton button = new JButton("New Parameters");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ControlGUI(thresh, pitchConsts);
			}
		});
		frame.getContentPane().add(label);
		frame.getContentPane().add(button,BorderLayout.SOUTH);
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
		label.getGraphics().drawImage(frame.getBufferedImage(), 0, 0, WIDTH,
				HEIGHT, null);
		frame.recycle();
	}
}