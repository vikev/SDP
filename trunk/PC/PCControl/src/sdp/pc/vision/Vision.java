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

	private static final int 
//		ANGLE_SMOOTHING_FRAME_COUNT = 4,	// ???
		VIDEO_STANDARD = V4L4JConstants.STANDARD_PAL, 
		CHANNEL = 0;
	private static final String DEVICE = "/dev/video0";

	
	static WorldState state = new WorldState();
	static WorldStateListener stateListener;
	private static Thread stateUpdaterThread;
	private WorldStatePainter statePainter;
	
//	public static Point2 requestedData = new Point2(-1, -1);
	private static PitchConstants pitchConsts = new PitchConstants(0);
	private static ThresholdsState thresh = new ThresholdsState();

	public static JLabel frameLabel;
	public static JFrame frame;

	private static VideoDevice videoDevice;
	private static FrameGrabber frameGrabber;

	//TODO: what were those used for?
//	private static double[][] angleSmoothing = new double[4][ANGLE_SMOOTHING_FRAME_COUNT];
//	private static int angSmoothingWriteIndex = 0;

	
	
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
		//set state
		Vision.state = state;
		
		//create state listener
		stateListener = new WorldStateUpdater(200, state);
		Vision.stateUpdaterThread = new Thread(stateListener);
		Vision.stateUpdaterThread.setDaemon(true);
		stateUpdaterThread.start();
		
		//create state painter
		statePainter = new WorldStatePainter(stateListener, state);
		
		//load constants
		pitchConsts.loadConstants("pitch0");
		pitchConsts.uploadConstants(thresh);
		Colors.setTreshold(thresh);

		//initialise the frame fetcher
		try {
			initFrameGrabber();
		} catch (V4L4JException e1) {
			System.err.println("Error setting up capture");
			e1.printStackTrace();
			cleanupCapture();
			return;
		}

		//create the form
		initGUI();

		//set the frame callback
		frameGrabber.setCaptureCallback(new CaptureCallback() {
			//used to prevent flickering
			BufferedImage buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
			
			//exception handling
			public void exceptionReceived(V4L4JException e) {
				System.err.println("Unable to capture frame:");
				e.printStackTrace();
			}
			
			//new frame!
			public void nextFrame(VideoFrame frame) {
				//grab the new frame
				BufferedImage frameImage = frame.getBufferedImage();
				frame.recycle();
				
				//notify the listener
				stateListener.setCurrentFrame(frameImage);
				
				//copy the new frame to the buffer (overwriting anything that was there already)
				Graphics bg = buffer.getGraphics();
				bg.drawImage(frameImage, 0, 0, WIDTH, HEIGHT, null);
				bg.dispose();
				
				//draw the world overlay to the buffer
				Point2 mousePos = new Point2(Vision.frame.getContentPane().getMousePosition());//.subtractBorders();
				statePainter.drawWorld(buffer, mousePos);
				
				//draw the result to the frameLabel
				Graphics labelG = frameLabel.getGraphics();
				labelG.drawImage(buffer, 0, 0, WIDTH, HEIGHT, null);
				labelG.dispose();
			}
		});

		frameGrabber.startCapture();

		//add the mouse Listener
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