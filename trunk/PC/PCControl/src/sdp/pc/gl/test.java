package sdp.pc.gl;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opencl.*;
import org.lwjgl.util.glu.GLU;

import sdp.pc.vision.Vision;

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.StateException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

public class test {
	/**
	 * I've adjusted it to 100 and manually taken a look at the performance
	 * while running Vision. It doesn't seem to change. As long as no one else
	 * has problems we should keep it at 100
	 */
	private static final int JPEG_QUALITY = 100;

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
	public static final String DEVICE = "/dev/video0";
	/**
	 * The object which V4L4J uses to grab individual frames. Mostly abstracted.
	 */
	private static FrameGrabber frameGrabber;
	
	/**
	 * A V4L4J instance for grabbing video. Fairly abstract.
	 */
	private static VideoDevice videoDevice;
	
	
	private static GLObjectTracker tracker;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		setupFrameGrabber();
		
		tracker = new GLObjectTracker();
		

		tracker.run();
		frameGrabber.stopCapture();
	}

	private static void setupFrameGrabber() {
		try {
			videoDevice = new VideoDevice(DEVICE);
			
			frameGrabber = videoDevice.getJPEGFrameGrabber(WIDTH, HEIGHT, CHANNEL,
					VIDEO_STANDARD, JPEG_QUALITY);
			
			frameGrabber.setCaptureCallback(new CaptureCallback() {
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
					tracker.setLastFrame(frameImage);
				}
			});
			
			frameGrabber.startCapture();
		} catch (Exception e1) {
			System.err.println("Error setting up capture");
			e1.printStackTrace();
			try {
				frameGrabber.stopCapture();
			} catch (StateException ex) {
			}
			videoDevice.releaseFrameGrabber();
			videoDevice.release();
			return;
		}		
	}



}
