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
 * This code builds a JFrame that shows the feed from the camera and
 * calculates and displays positions of objects on the field.
 * Part of the code is inspired from group 1 of SDP 2013.
 * Most important method is processImage - don't touch stuff like initGui unless need be
 * 
 * @author Borislav Ikonomov, Group 8, SDP 2014
 *
 */
public class Vision extends WindowAdapter implements CaptureCallback{
		// Camera and image parameters
        private static int      width = 640, height = 480, std = V4L4JConstants.STANDARD_PAL, channel = 0;
        private static String   device = "/dev/video0";

        // Other globals
        private VideoDevice     videoDevice;
        private FrameGrabber    frameGrabber;
        private JLabel          label;
        private JFrame          frame;
        
        long before = 0; //For FPS


        public static void main(String args[]){
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
         * @throws V4L4JException if any parameter if invalid
         */
        public Vision() throws V4L4JException{
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
                        before = System.currentTimeMillis();  // for FPS
                        BufferedImage frameImage = frame.getBufferedImage();
                        frame.recycle();
                        processImage(frameImage);
                    }
                });
                
                frameGrabber.startCapture();
                
        }
               
        /**
         * Finds locations of balls and robots and makes the graphical image
         * (still largely incomplete; only finds ball at this stage)
         * @param image
         */
        private void processImage(final BufferedImage image) {

        	int ballX = 0;
            int ballY = 0;
            int numBallPos = 0;
            ArrayList<Integer> ballXPoints = new ArrayList<Integer>();
            ArrayList<Integer> ballYPoints = new ArrayList<Integer>();
            
            // Checks every pixel
            int ballPix =0; // for debugging
        	for (int row = 0; row < image.getHeight() - 0; row++) {    // Both loops need to start from table edges
                for (int column = 0; column < image.getWidth() - 0; column++) { // rather than image edges (0)
                    // RGB colour scheme
                	Color c = new Color(image.getRGB(column, row));
				    // HSB colour scheme (unused atm)
                	float hsbvals[] = new float[3];
                    Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
                	
                    // Find "Ball" pixels
                	if (c.getRed()>140 && c.getBlue()<45 && c.getGreen()<45) {
                		
                		ballPix++;
                		
                        ballX += column;
                        ballY += row;
                        numBallPos++;

                        ballXPoints.add(column);
                        ballYPoints.add(row);

                       
                    }
                }
        	}
        	
        	System.out.println("Ball pixels: " + ballPix);
        	// Get average position of ball
        	if (numBallPos!=0) {
        		ballX /= numBallPos;
        		ballY /= numBallPos;
        	}
        	
        	
        	/* Create graphical representation */
        	Graphics imageGraphics = image.getGraphics();
        	Graphics frameGraphics = label.getGraphics();
        	imageGraphics.setColor(Color.red);
            imageGraphics.drawLine(0, ballY, 640, ballY);
            imageGraphics.drawLine(ballX, 0, ballX, 480);
        	imageGraphics.drawImage(image, 0, 0, width, height, null);
        	
        	 /* Used to calculate the FPS. */
            long after = System.currentTimeMillis();

            /* Display the FPS that the vision system is running at. */
            float fps = (1.0f)/((after - before) / 1000.0f);
            imageGraphics.setColor(Color.white);
            imageGraphics.drawString("FPS: " + fps, 15, 15);
            frameGraphics.drawImage(image, 0, 0, width, height, null);
        }
        
        
        /**
         * Initialises a FrameGrabber object with the given parameters.
         * @param videoDevice           The video device file to capture from.
         * @param inWidth               The desired capture width.
         * @param inHeight              The desired capture height.
         * @param channel               The capture channel.
         * @param videoStandard         The capture standard.
         * @param compressionQuality    The JPEG compression quality.
         * 
         * @throws V4L4JException   If any parameter is invalid.
         */
        private void initFrameGrabber() throws V4L4JException{
                videoDevice = new VideoDevice(device);
                frameGrabber = videoDevice.getJPEGFrameGrabber(width, height, channel, std, 80);
                frameGrabber.setCaptureCallback(this);
                width = frameGrabber.getWidth();
                height = frameGrabber.getHeight();
                System.out.println("Starting capture at "+width+"x"+height);
        }

        /** 
         * Creates the UI components (the JFrame) and initialises them
         */
        private void initGUI(){
                frame = new JFrame();
                label = new JLabel();
                frame.getContentPane().add(label);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.addWindowListener(this);
                frame.setVisible(true);
                frame.setSize(width, height);       
        }
        
        /**
         * This method stops the capture and releases the frame grabber and video device
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
                label.getGraphics().drawImage(frame.getBufferedImage(), 0, 0, width, height, null);
                frame.recycle();
        }
}
