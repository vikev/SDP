package sdp.pc.vision;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import sdp.pc.vision.settings.Calibration;
import sdp.pc.vision.settings.ControlGUI;
import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.StateException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

/**
 * Builds a JFrame that shows the feed from the camera and instantiates
 * necessary world state models. Depending on states and other logic data,
 * displays overlay data as well.
 * 
 * An instance of Vision should be initialised as the parent for all camera feed
 * data. EG Milestone3att.java initialises a new Vision.
 * 
 */
public class Vision extends WindowAdapter implements CaptureCallback {

	/**
	 * The desired (maximum?) FPS at which the world state will refresh
	 */
	private static final int WORLD_STATE_TARGET_FPS = 60;

	/**
	 * Width of the video feed
	 */
	static final int WIDTH = 640;

	/**
	 * Height of the video feed
	 */
	static final int HEIGHT = 480;

	/**
	 * Size of the buffer for smoothing orientation (disabled)
	 */
	// private static final int ANGLE_SMOOTHING_FRAME_COUNT = 4;

	/**
	 * Video standard used by V4L4J (should be PAL)
	 */
	private static final int VIDEO_STANDARD = V4L4JConstants.STANDARD_PAL;

	/**
	 * Which channel to get the video feed from (should be 0)
	 */
	private static final int CHANNEL = 0;

	/**
	 * The device driver name for the video feed (should be /dev/video0 for both
	 * pitches --> don't change it)
	 */
	private static final String DEVICE = "/dev/video0";

	/**
	 * The world state used by the Vision system. There should be one set of
	 * world state objects for a Vision instance.
	 */
	static WorldState state = new WorldState();

	/**
	 * An abstraction of the world state used to get state details
	 */
	public static WorldStateListener stateListener;

	/**
	 * Thread which runs the world state updater
	 */
	private static Thread stateUpdaterThread;

	/**
	 * Painter which draws any highlighted pixels or control data to the vision
	 * stream
	 */
	private WorldStatePainter statePainter;

	/**
	 * PitchConstants holds discrete values like thresholds and important
	 * points. 0 refers to the main pitch, while 1 would be the side pitch.
	 * 
	 * TODO:This should be abstracted at some point
	 */
	private static PitchConstants pitchConsts = new PitchConstants();

	/**
	 * A massive list of getters and setters for requesting threshold values
	 */
	private static ThresholdsState thresh = new ThresholdsState();

	/**
	 * Main label for the video feed frame
	 */
	public static JLabel frameLabel;

	/**
	 * Main frame for the video feed
	 */
	public static JFrame frame;

	/**
	 * A V4L4J instance for grabbing video. Fairly abstract.
	 */
	private static VideoDevice videoDevice;

	/**
	 * The object which V4L4J uses to grab individual frames. Mostly abstracted.
	 */
	private static FrameGrabber frameGrabber;

	/**
	 * angleSmoothing was a system which buffered orientation values to smooth
	 * the facing angles of our robots. It's disabled now because the new
	 * orientation code is more accurate, but it may be useful to smooth 2-3
	 * frames if our orientation gets jumpy.
	 */
	// private static double[][] angleSmoothing = new
	// double[4][ANGLE_SMOOTHING_FRAME_COUNT];
	// private static int angSmoothingWriteIndex = 0;

	/**
	 * Provides Java application support. On launch, runs a JFrame window which
	 * displays the video feed with our overlaid data.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		/**
		 * Anyone know why we use invokeLater to start the video feed? Does this
		 * simply mean "run after swing has initialised the frame"?
		 */
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
	 * Builds the WebcamViewer object and initialises state data and thresholds
	 * 
	 * @param state
	 *            the WorldState which will be updated with all the information
	 *            from the vision system dynamically.
	 * 
	 * @throws V4L4JException
	 *             if any parameter is invalid
	 */
	public Vision(WorldState state) throws V4L4JException {
		// Set state
		Vision.state = state;

		// Create state listener
		stateListener = new WorldStateUpdater(WORLD_STATE_TARGET_FPS, state);
		Vision.stateUpdaterThread = new Thread(stateListener);
		Vision.stateUpdaterThread.setDaemon(true);
		stateUpdaterThread.start();

		// create state painter
		statePainter = new WorldStatePainter(stateListener, state);

		// Load threshold and point constants. TODO: Abstract this to not refer
		// specifically to pitch0.
		pitchConsts.loadConstantsForPitchUsedLastTime();
		pitchConsts.uploadConstants(thresh, state);
		Colors.setTreshold(thresh); 
		
		// Initialise the frame fetcher
		try {
			initFrameGrabber();
		} catch (V4L4JException e1) {
			System.err.println("Error setting up capture");
			e1.printStackTrace();
			cleanupCapture();
			return;
		}
		
		// Create the form
		initGUI();
		

		
		/**
		 * Set the frame callback with the frame grabber. Executes for every new
		 * frame.
		 */
		frameGrabber.setCaptureCallback(new CaptureCallback() {
			// Used to prevent flickering
			BufferedImage buffer = new BufferedImage(WIDTH, HEIGHT,
					BufferedImage.TYPE_3BYTE_BGR);

			/**
			 * Exception handling
			 */
			public void exceptionReceived(V4L4JException e) {
				System.err.println("Unable to capture frame:");
				e.printStackTrace();
			}

			/**
			 * The nextFrame handler
			 */
			public void nextFrame(VideoFrame frame) {
				// Grab the new frame
				BufferedImage frameImage = frame.getBufferedImage();
				frame.recycle();

				// Notify the listener
				stateListener.setCurrentFrame(frameImage);

				// Copy the new frame to the buffer (overwriting anything that
				// was there already)
				Graphics bg = buffer.getGraphics();
				bg.drawImage(frameImage, 0, 0, WIDTH, HEIGHT, null);
				bg.dispose();

				// Draw the world overlay to the buffer
				Point2 mousePos = new Point2(Vision.frame.getContentPane()
						.getMousePosition());
				// TODO: Commented out was '.subtractBorders();' - do we need
				// this?
				statePainter.drawWorld(buffer, mousePos);

				// Draw the result to the frameLabel
				Graphics labelG = frameLabel.getGraphics();
				labelG.drawImage(buffer, 0, 0, WIDTH, HEIGHT, null);
				labelG.dispose();
			}
		});

		// Begin video capture
		frameGrabber.startCapture();

		// Add the mouse Listener
		frame.addMouseListener(new Calibration());
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

		// TODO: Has anyone tried changing the JPEG quality? JPEG can be pushed
		// all the way to 100, which may improve our pixel recognition (at the
		// cost of performance?) If it makes any difference, JPEG quality should
		// be abstracted to a constant.
		frameGrabber = videoDevice.getJPEGFrameGrabber(WIDTH, HEIGHT, CHANNEL,
				VIDEO_STANDARD, 80);

		frameGrabber.setCaptureCallback(this);
	}

	/**
	 * Creates the UI components (the JFrame) and initialises them
	 */
	private void initGUI() {
		frame = new JFrame();
		frameLabel = new JLabel();

		// Add button below the frame for opening the settings menu
		JButton button = new JButton("Settings");
		button.addActionListener(new ActionListener() {

			/**
			 * Open the control GUI on click
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				new ControlGUI(thresh, statePainter, pitchConsts);
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