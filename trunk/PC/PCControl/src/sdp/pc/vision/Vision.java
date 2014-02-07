package sdp.pc.vision;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import sdp.pc.common.Constants;
import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.FrameGrabber;
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
	static final int WIDTH = 640;
	static final int HEIGHT = 480;

	private static final int VIDEO_STANDARD = V4L4JConstants.STANDARD_PAL, CHANNEL = 0, ANGLE_SMOOTHING_FRAME_COUNT = 4;

	private static final String DEVICE = "/dev/video0";

	private static int angSmoothingWriteIndex = 0;
	private static WorldState state = new WorldState();
	private static WorldStateListener stateListener;
	private static Thread stateUpdaterThread;
	public static Point2 requestedData = new Point2(-1, -1);
	private static PitchConstants pitchConsts = new PitchConstants(0);
	private static ThresholdsState thresh = new ThresholdsState();

	private static JLabel label;
	public static JFrame frame;

	protected WorldStatePainter statePainter;
	private static VideoDevice videoDevice;
	private static FrameGrabber frameGrabber;
	public static boolean edgesCalibrated = false;

	// Used for calculating direction the ball is heading
	static Point2[] prevFramePos = new Point2[10];
=======
	// Vision-specific paramters
	private static final int WIDTH = 640, HEIGHT = 480,
			VIDEO_STANDARD = V4L4JConstants.STANDARD_PAL, CHANNEL = 0,
			PLAYER_RADIUS = 18, FRAME_IGNORE_COUNT = 50,
			ANGLE_SMOOTHING_FRAME_COUNT = 4, MIN_POINTS_BOT = 10;
	private static final String DEVICE = "/dev/video0";
	private static final double VECTOR_THRESHOLD = 5.0;

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
	public static float fps = 1;
	public static ArrayList<Point2> pitchPoints = new ArrayList<Point2>();
	public static WorldState state = new WorldState();
	public static Point2 requestedData = new Point2(-1, -1);
	public static JFrame frame;
	public static HashMap<Integer, Integer> hullPoints = new HashMap<Integer, Integer>();
	public static boolean hullCalculated = false;
	public static boolean edgesCalibrated;
	public static BufferedImage frameImage;
>>>>>>> f0353d1dedd0e828fab637caa91faa3541463228

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

		//set state
		Vision.state = state;
		
		//create state listener
		stateListener = new WorldStateUpdater(60, state);
		Vision.stateUpdaterThread = new Thread(stateListener);
		Vision.stateUpdaterThread.setDaemon(true);
		stateUpdaterThread.start();
		
		//create state painter
		statePainter = new WorldStatePainter(stateListener, state);
		
		//load constants
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
			BufferedImage buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
			public void nextFrame(VideoFrame frame) {
				BufferedImage frameImage = frame.getBufferedImage();
				frame.recycle();
				
				stateListener.setCurrentImage(frameImage);
				
				//copy the new image to the buffer
				Graphics bg = buffer.getGraphics();
				bg.drawImage(frameImage, 0, 0, WIDTH, HEIGHT, null);
				bg.dispose();
				
				//draw the overlay
				Point2 mousePos = new Point2(Vision.frame.getContentPane().getMousePosition());//.subtractBorders();
				statePainter.drawWorld(buffer, mousePos);
				
				//copy the result to the screen
				Graphics labelG = label.getGraphics();
				labelG.drawImage(buffer, 0, 0, WIDTH, HEIGHT, null);
				labelG.dispose();
			}
		});

		frameGrabber.startCapture();

		// Mouse Listener
		frame.addMouseListener(new Calibration(stateListener));
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
		JButton button = new JButton("Settings");
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
		frameLabel.getGraphics().drawImage(frame.getBufferedImage(), 0, 0,
				WIDTH, HEIGHT, null);
		frame.recycle();
	}
}