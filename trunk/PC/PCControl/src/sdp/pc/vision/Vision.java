package sdp.pc.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

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
	private static int width = 640, height = 480,
			std = V4L4JConstants.STANDARD_PAL, channel = 0;
	private static String device = "/dev/video0";

	// Other globals
	private VideoDevice videoDevice;
	private FrameGrabber frameGrabber;
	private JLabel label;
	private JFrame frame;

	long before = 0; // For FPS
 
	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new Vision();
				} catch (V4L4JException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Builds a WebcamViewer object
	 * 
	 * @throws V4L4JException
	 *             if any parameter if invalid
	 */
	public Vision() throws V4L4JException {
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
				before = System.currentTimeMillis(); // for FPS
				BufferedImage frameImage = frame.getBufferedImage();
				frame.recycle();
				processImage(frameImage);
			}
		});

		frameGrabber.startCapture();

	}

	// Used for calculating direction the ball is heading
	int[] prevFramePosX = new int[10], prevFramePosY = new int[10];

	/**
	 * Finds locations of balls and robots and makes the graphical image (still
	 * largely incomplete; only finds ball at this stage)
	 * 
	 * @param image
	 */
	private void processImage(BufferedImage image) {

		int ballX = 0;
		int ballY = 0;
		int numBallPos = 0;
		int yellowX = 0;
		int yellowY = 0;
		int numYellowPos = 0;
		ArrayList<Integer> yellowXPoints = new ArrayList<Integer>();
        ArrayList<Integer> yellowYPoints = new ArrayList<Integer>();
		int blueX = 0;
		int blueY = 0;
		int numBluePos = 0;

		// Checks every pixel
		@SuppressWarnings("unused")
		int ballPix = 0; int yellowPix=0; int bluePix =0; // for debugging
			
		// Both loops need to start from table edges rather than image edges (0)
		for (int row = 0; row < image.getHeight() - 0; row++) { 
			for (int column = 0; column < image.getWidth() - 0; column++) {
				// RGB colour scheme
				Color c = new Color(image.getRGB(column, row));
				// HSB colour scheme (unused atm)
				float hsbvals[] = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);

				// Find "Ball" pixels
				if (c.getRed() > 160 && c.getBlue() < 20 && c.getGreen() < 20) {
					//ballPix++;
					ballX += column;
					ballY += row;
					numBallPos++;
				}
				
				// Find Yellow pixels
				if (c.getRed() > 180 && c.getBlue() < 5 && c.getGreen()>60 && c.getGreen() < 110) {
					yellowPix++;
					yellowX += column;
					yellowY += row;
					numYellowPos++;
			        image.setRGB(column, row, Color.ORANGE.getRGB()); //Makes yellow pixels orange
			        yellowXPoints.add(column);
                    yellowYPoints.add(row);
				}
				
				// Find Blue pixels - sucks atm
				if (c.getRed() > 40 && c.getRed() < 48 &&
						c.getBlue() > 80 && c.getBlue() < 90 && 
						c.getGreen()>65 && c.getGreen() < 75) {
					bluePix++;
					blueX += column;
					blueY += row;
					numBluePos++;
			        //image.setRGB(column, row, Color.ORANGE.getRGB()); //Makes blue pixels orange
				}
			}
		}

		//System.out.println("Yellow pixels: " + yellowPix);
		
		// Get average position of ball
		if (numBallPos != 0) {
			ballX /= numBallPos;
			ballY /= numBallPos;
		}
		//Get average position of yellow bot
		if (numYellowPos != 0) {
			yellowX /= numYellowPos;
			yellowY /= numYellowPos;
		}
		//Get average position of blue bot
		if (numBluePos != 0) {
			blueX /= numBluePos;
			blueY /= numBluePos;
		}
		
		// Calculates where ball is going
		int avgPrevPosX = 0;
		for (int i : prevFramePosX) avgPrevPosX+=i;
		avgPrevPosX/=prevFramePosX.length;
		int avgPrevPosY = 0;
		for (int i : prevFramePosY) avgPrevPosY+=i;
		avgPrevPosY/=prevFramePosY.length;
	
		avgPrevPosX += 10 * (ballX - avgPrevPosX);
		avgPrevPosY += 10 * (ballY - avgPrevPosY);
		
		if (!yellowXPoints.isEmpty() || !yellowYPoints.isEmpty())
			findOrientation(yellowXPoints, yellowYPoints, yellowX, yellowY, image, true);

		/* Create graphical representation */
		Graphics imageGraphics = image.getGraphics();
		Graphics frameGraphics = label.getGraphics();
		// Ball location (and direction)
		imageGraphics.setColor(Color.red);
		imageGraphics.drawLine(0, ballY, 640, ballY);
		imageGraphics.drawLine(ballX, 0, ballX, 480);
		imageGraphics.drawLine(ballX, ballY, avgPrevPosX, avgPrevPosY);
		// Yellow robots locations
		imageGraphics.setColor(Color.yellow);
		imageGraphics.drawOval(yellowX-15, yellowY-15, 30,30);
		imageGraphics.setColor(Color.black);
		imageGraphics.drawOval(yellowX-2, yellowY-2, 4,4);
		// Blue robots locations
		imageGraphics.setColor(Color.blue);
		//imageGraphics.drawOval(blueX-15, blueY-15, 30,30);
		
		imageGraphics.drawImage(image, 0, 0, width, height, null);
		
		// Saves this frame's ball position and shifts previous frames' positions
		for (int i=prevFramePosX.length-1; i>0 ; i--) {
			prevFramePosX[i] = prevFramePosX[i-1];
		}
		for (int i=prevFramePosY.length-1; i>0 ; i--) {
			prevFramePosY[i] = prevFramePosY[i-1];
		}
		prevFramePosX[0] = ballX;
		prevFramePosY[0] = ballY;
		
		/* Used to calculate the FPS. */
		long after = System.currentTimeMillis();

		/* Display the FPS that the vision system is running at. */
		float fps = (1.0f) / ((after - before) / 1000.0f);
		imageGraphics.setColor(Color.white);
		imageGraphics.drawString("FPS: " + fps, 15, 15);
		frameGraphics.drawImage(image, 0, 0, width, height, null);
		
		// Gives RGB values of the point the cursor is on
		if (frame.getMousePosition() != null
				&& frame.getMousePosition() != null) {
			int x = (int) Math.round(frame.getMousePosition().getX()) - 5;
			int y = (int) Math.round(frame.getMousePosition().getY()) - 23;

			imageGraphics.drawString("Mouse pos: x:" + x + " y:" + y, 15, 30);
			frameGraphics.drawImage(image, 0, 0, width, height, null);
			Color c = new Color(image.getRGB(x, y));
			imageGraphics.drawString(
					"Color: R:" + c.getRed() + " G:" + c.getGreen() + " B:"
							+ c.getBlue(), 15, 45);
			frameGraphics.drawImage(image, 0, 0, width, height, null);
		}
	}
	
	/**
     * Finds the orientation of a robot, given a list of the points contained within it's
     * T-shape (in terms of a list of x coordinates and y coordinates), the mean x and
     * y coordinates, and the image from which it was taken.
     *
     * @param xpoints           The x-coordinates of the points contained within the T-shape.
     * @param ypoints           The y-coordinates of the points contained within the T-shape.
     * @param meanX             The mean x-point of the T.
     * @param meanY             The mean y-point of the T.
     * @param image             The image from which the points were taken.
     * @param showImage         A boolean flag - if true a line will be drawn showing
     *                          the direction of orientation found.
     *
     * @return                  An orientation from -Pi to Pi degrees.
     * @throws NoAngleException
     */
	public float findOrientation(ArrayList<Integer> xpoints, ArrayList<Integer> ypoints,
            int meanX, int meanY, BufferedImage image, boolean showImage) {
        assert (xpoints.size() == ypoints.size()) :
            "Error: Must be equal number of x and y points!";
        
        /* Find the position of the front of the T. */
        int frontX = 0;
        int frontY = 0;
        int frontCount = 0;
        for (int i = 0; i < xpoints.size(); i++) {
        	// If pixel is certain distance from mean, add it to the "front"
        	if (sqrdEuclidDist(xpoints.get(i), ypoints.get(i), meanX, meanY) > Math.pow(12, 2)) {
        		frontCount++;
        		frontX += xpoints.get(i);
        		frontY += ypoints.get(i);
        		image.setRGB(xpoints.get(i), ypoints.get(i), Color.BLACK.getRGB());
        	}
        }
        
        // Set frontX and frontY to their average values
        if (frontCount!=0) {
        	frontX /= frontCount;
        	frontY /= frontCount;
        }
        
        // Calculate angle and 
        float length = (float) Math.sqrt(Math.pow(frontX - meanX, 2)
                + Math.pow(frontY - meanY, 2));
        float ax = (frontX - meanX) / length;
        float ay = (frontY - meanY) / length;
        float angle = (float) Math.acos(ax);

        if (frontY < meanY) {
            angle = -angle;
        }
        
        if (angle == 0) {
            return (float) 0.001;
        }
        
        if (showImage) {
            image.getGraphics().drawLine((int)frontX, (int)frontY, (int)(frontX+ax*70), (int)(frontY+ay*70));
            image.getGraphics().drawOval((int) frontX-4, (int) frontY-4, 8, 8);
        }

        return angle;
        
	}	
	public static float sqrdEuclidDist(int x1, int y1, int x2, int y2) {
		return (float) (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
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
	 * @param channel
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
		videoDevice = new VideoDevice(device);
		frameGrabber = videoDevice.getJPEGFrameGrabber(width, height, channel,
				std, 80);
		frameGrabber.setCaptureCallback(this);
		width = frameGrabber.getWidth();
		height = frameGrabber.getHeight();
		System.out.println("Starting capture at " + width + "x" + height);
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
		frame.setSize(width, height);
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
		label.getGraphics().drawImage(frame.getBufferedImage(), 0, 0, width,
				height, null);
		frame.recycle();
	}
}