package sdp.pc.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
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
	private static final int width = 640, height = 480,
			std = V4L4JConstants.STANDARD_PAL, channel = 0,
			X_FRAME_OFFSET = 1, Y_FRAME_OFFSET = 25;
	private static String device = "/dev/video0";

	// Other globals
	private VideoDevice videoDevice;
	private FrameGrabber frameGrabber;
	private JLabel label;
	private JFrame frame;
	
	private WorldState state;

	long before = 0; // For FPS
 
	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new Vision(new WorldState());
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
	public Vision(WorldState state) throws V4L4JException {
		this.state = state;
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
		
		ArrayList<Position> yellowPoints = new ArrayList<Position>();
		ArrayList<Position> bluePoints = new ArrayList<Position>();

		// Checks every pixel
		@SuppressWarnings("unused")
		int ballPix = 0; int yellowPix=0; int bluePix =0; // for debugging
		
		// Both loops need to start from table edges rather than image edges (0)
		for (int row = 80; row < image.getHeight() - 80; row++) { 
			for (int column = 50; column < image.getWidth() - 50; column++) {
				// RGB colour scheme
				Color c = new Color(image.getRGB(column, row));
				// HSB colour scheme (unused atm)
				float hsbvals[] = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);

				// Find "Ball" pixels
				if (isBall(c)) {
					//ballPix++;
					ballX += column;
					ballY += row;
					numBallPos++;
					//image.setRGB(column, row, Color.ORANGE.getRGB()); //Makes red pixels orange
				}
				
				// Find Yellow pixels
				if (isYellow(c)) {
					yellowPix++;
					yellowX += column;
					yellowY += row;
					numYellowPos++;
			        //image.setRGB(column, row, Color.ORANGE.getRGB()); //Makes yellow pixels orange
			        yellowXPoints.add(column);
                    yellowYPoints.add(row);
				}
				
				// Find Blue pixels - sucks atm
				if (isBlue(c)) {
					bluePix++;
					blueX += column;
					blueY += row;
					numBluePos++;
					/*
					if (groupCheckBlue(image, column, row)){
						Position p = new Position(column,row);
						bluePoints.add(p);
						image.setRGB(column, row, Color.WHITE.getRGB());
					}
					*/
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
		/*int maxScore = 0;
		for (Point p : bluePoints) {
			int score = 0;
			for (int i=-5; i<5; i++) for (int j=-5; j<5; j++) {
				System.out.println(p.getX());
				System.out.println(p.getY());
				Color c = new Color(image.getRGB((int) p.getX()+i,(int)  p.getY()+j));
				if (isBlue(c)) {
					score++;
				}
			}
			if (score>maxScore) {
				maxScore = score;
				blueX = p.x; blueY = p.y;
			}
		}*/
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
		
		double yellowOrientation = 0;
		if (!yellowXPoints.isEmpty() || !yellowYPoints.isEmpty())
			yellowOrientation = findOrientation(yellowXPoints, yellowYPoints, yellowX, yellowY, image, true);
		
		// Update World State
		state.setBallX(ballX);
		state.setBallY(ballY);
		state.setYellowX(yellowX);
		state.setYellowY(yellowY);
		state.setYellowOrientation(yellowOrientation);
		

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
		// Blue robots locations
		imageGraphics.setColor(Color.blue);
		imageGraphics.drawOval(blueX-15, blueY-15, 30,30);
		
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
		int x=1, y=0;
		Point pos;
		
		// Gives RGB values of the point the cursor is on
		if (frame.getMousePosition() != null
				&& frame.getMousePosition() != null) {
			pos = frame.getMousePosition();
			x = (int) Math.round(pos.getX()) - X_FRAME_OFFSET;
			y = (int) Math.round(pos.getY()) - Y_FRAME_OFFSET;
			if(x>0 && x<= (width-X_FRAME_OFFSET) && y<= (height-Y_FRAME_OFFSET)){
				
	
				imageGraphics.drawString("Mouse pos: x:" + x + " y:" + y, 15, 30);
				frameGraphics.drawImage(image, 0, 0, width, height, null);
				
				Color c = new Color(image.getRGB(x, y));
				
				imageGraphics.drawString(
						"Color: R:" + c.getRed() + " G:" + c.getGreen() + " B:"
								+ c.getBlue(), 15, 45);
				float[] hsb = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsb);
				imageGraphics.drawString(
						"HSB: H:" + new DecimalFormat("#.###").format(hsb[0]) + 
						" S:" + new DecimalFormat("#.###").format(hsb[1]) + 
						" B:" + new DecimalFormat("#.###").format(hsb[2]), 15, 60);
				frameGraphics.drawImage(image, 0, 0, width, height, null);
			}
		}
	}
	
	/**
     * Determines if a pixel is part of the ball, based on input RGB colours
     * and hsv values.
     *
     * @param color         The RGB colours for the pixel.
     * @param hsbvals       The HSV values for the pixel.
     *
     * @return              True if the RGB and HSV values are within the defined
     *                      thresholds (and thus the pixel is part of the ball),
     *                      false otherwise.
     */
    private boolean isBall(Color c) {
        return (c.getRed() > 130 && c.getGreen() < 30 && c.getBlue() < 30);
    }
	
    /**
     * Determines if a pixel is part of the yellow T, based on input RGB colours
     * and hsv values.
     *
     * @param color         The RGB colours for the pixel.
     * @param hsbvals       The HSV values for the pixel.
     *
     * @return              True if the RGB and HSV values are within the defined
     *                      thresholds (and thus the pixel is part of the yellow T),
     *                      false otherwise.
     */
    private boolean isYellow(Color c) {
    	return (c.getRed() > 170 && c.getBlue() < 5 && c.getGreen()>60 && c.getGreen() < 100);
    }
    
    /**
     * Determines if a pixel is part of the blue T, based on input RGB colours
     * and hsv values.
     *
     * @param color         The RGB colours for the pixel.
     * @param hsbvals       The HSV values for the pixel.
     *
     * @return              True if the RGB and HSV values are within the defined
     *                      thresholds (and thus the pixel is part of the blue T),
     *                      false otherwise.
     */
	private boolean isBlue(Color c) {
		return (c.getRed() > 40 && c.getRed() <60 &&
				c.getBlue() > 80 && c.getBlue() < 120 && 
				c.getGreen()>55 && c.getGreen() < 75);
	}
	
	private boolean groupCheckBlue(BufferedImage image, int row,int column){
		Color b = new Color(image.getRGB(column-1, row));
		Color c = new Color(image.getRGB(column, row));
		Color d = new Color(image.getRGB(column+1, row));
		if (isBlue(b) && isBlue(c) && isBlue(d)){
			return true;
		}
		else{
			return false;
		}
	}
	
	double prevBestAngle = 0;
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
	public double findOrientation(ArrayList<Integer> xpoints, ArrayList<Integer> ypoints,
            int meanX, int meanY, BufferedImage image, boolean showImage) {
		double angle;
		double goodAngle=0;
		int goodAngleCount=0;
		
		for (angle=0 ; angle<360; angle++) {
			int newCentX = meanX + (int) Math.round(4*Math.cos(angle*2*Math.PI/360));
			int newCentY = meanY + (int) Math.round(4*Math.sin(angle*2*Math.PI/360));
			int yellowCountBack=0;
			int yellowCountSides=0;
			int yellowCountFront=0;
			
			// Goes to back of bot
			for (int i=0; i<20; i++) {
				int x = newCentX + (int) Math.round(i*Math.cos(angle*2*Math.PI/360));
				int y = newCentY + (int) Math.round(i*Math.sin(angle*2*Math.PI/360));
				Color c = new Color(image.getRGB(x,y));
				if (isYellow(c)) {
					yellowCountBack++;
				}
			}
			// Goes to sides of bot
			for (int i=-20; i<20; i++) {
				int x = newCentX + (int) Math.round(i*Math.cos((angle+90)*2*Math.PI/360));
				int y = newCentY + (int) Math.round(i*Math.sin((angle+90)*2*Math.PI/360));
				Color c = new Color(image.getRGB(x,y));
				if (isYellow(c)) {
					yellowCountSides++;
				}
			}
			
			// Goes to front of bot
			for (int i=0; i<25; i++) {
				int x = newCentX + (int) Math.round(i*Math.cos((angle+180)*2*Math.PI/360));
				int y = newCentY + (int) Math.round(i*Math.sin((angle+180)*2*Math.PI/360));
				Color c = new Color(image.getRGB(x,y));
				if (isYellow(c)) {
					yellowCountFront++;
				}
			}

			// Checks if angle is good 
			//(note the thresholds change constantly during a 24-hour cycle due to lighting)
			if (yellowCountBack >= 2 && yellowCountBack <= 4 && 
					yellowCountSides >= 20 && yellowCountFront >= 18) {
				goodAngle+=angle;
				goodAngleCount++;
				//System.out.println(yellowCountBack + " " + yellowCountSides + " " + yellowCountFront);
			}
			
		}
		
		if (goodAngleCount!=0) {
			goodAngle /= goodAngleCount;
			goodAngle +=180;
			if (goodAngle>360) goodAngle-=360;
			goodAngle = 360 - goodAngle;
		}
		else goodAngle = prevBestAngle;
		
		int x = meanX + (int) Math.round(10*Math.cos(goodAngle*2*Math.PI/360));
		int y = meanY + (int) Math.round(-10*Math.sin(goodAngle*2*Math.PI/360));
		image.getGraphics().setColor(Color.black);
		image.getGraphics().drawOval(x-2, y-2, 4,4);

		prevBestAngle = goodAngle;
		return goodAngle;
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
		//width = frameGrabber.getWidth();
		//height = frameGrabber.getHeight();
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