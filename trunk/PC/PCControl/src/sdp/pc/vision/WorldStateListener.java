package sdp.pc.vision;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * A generic world state updater. 
 * <p>
 * Implements methods for maintaining a thread alive (via the Runnable interface), 
 * checking for changes (considering preprocessing, boundaries, max FPS)
 * and the actual preprocessing (pitch hull calculation)
 * <p>
 * Provides abstract methods for image processing 
 * (the extraction of pixel data from the latest image)
 * and world updating (self explanatory)
 * that should be implemented in a derived class
 * 
 * @author s1141301
 *
 */
public abstract class WorldStateListener implements Runnable {

	/**
	 * We shall not sleep (pause) the thread if the amount is less than that
	 */
	private static final long SLEEP_ACCURACY = 5;
	/**
	 * The amount of frames to drop before preprocessing should occur
	 */
	private static final int FRAME_IGNORE_COUNT = 50;
	
	//boundary points
	private static Point2[] boundaryPoints = new Point2[] {
		new Point2(),
		new Point2()
	};

	/**
	 * the last frame we observed; used to prevent unnecessary updates
	 */
	private BufferedImage lastFrame;
	/**
	 * the latest frame we observed, hopefully reflecting the world as it is now
	 */
	private BufferedImage currentFrame;
	
	//colour buffers
	private Color[][] currentRgb = new Color[700][520];
	private float[][][] currentHsb = new float[700][520][3];

	/**
	 * Whether preprocessing (colour normalising) has occurred
	 */
	private boolean preprocessed = false;
	
	/**
	 * Keeps track of the frames skipped so far. Should become equal to FRAME_IGNORE_COUNT 
	 * before preprocessing takes place
	 */
	private int keyframe = 0;
	
	//fps measuring
	/**
	 * The time actually taken to calculate the last frame, in milliseconds
	 */
	private long currentFrameTime = 1;	//div by zero!
	/**
	 * The time scheduled for each frame, in milliseconds
	 */
	private final long targetFrameTime;
	
	//protected stuff usable by kidz of the class
	/**
	 * The world state we are supposed to update...
	 */
	protected final WorldState state;
	/**
	 * Contains all points considered as the pitch
	 */
	protected HashSet<Point2> pitchPoints = new HashSet<Point2>();
	
	
	/**
	 * Holds { min-observed-brightness, max-observed-brightness }
	 */
	protected float[] minMaxBrightness = new float[] { 1, 0 };
	
	/**
	 * Adds a boundary point for image clipping
	 * @param p the point to add
	 * @return whether the point was needed'n'received
	 */
	public static boolean addBoundary(Point2 p) {
		if(boundaryPoints[0].equals(Point2.Empty)) {
			boundaryPoints[0] = p;
			return true;
		}
		if(boundaryPoints[1].equals(Point2.Empty)) {
			boundaryPoints[1] = p;
		}
		return false;
	}
	
	/**
	 * Returns whether there's full boundary information
	 */
	public static boolean hasBoundary() {
		return !(boundaryPoints[0].equals(Point2.Empty) || boundaryPoints[1].equals(Point2.Empty));
	}
	
	/**
	 * Gets the selected point from the boundary.
	 * Makes no guarantees which point has smaller coordinates
	 * Returns an empty point if there is no boundary information for this index
	 * @param i the index of the point; should be in the range [0;1]
	 * @return the requested boundary point
	 */
	public static Point2 getBoundary(int i) {
		return boundaryPoints[i];
	}
	
	/**
	 * Resets the region boundary. 
	 */
	public static void resetBoundary() {
		boundaryPoints[0] = new Point2();
		boundaryPoints[1] = new Point2();
	}

	/**
	 * Returns whether preprocessing has occurred,
	 * i.e. whether the pitch hull and points are calculated
	 */
	public boolean isPreprocessed() {
		return preprocessed;
	}
	
	/**
	 * Returns all points considered as being on the pitch
	 * Undefined when hasBoundary() or isPreprocessed() returns false
	 */
	public Iterable<Point2> getPitchPoints() {
		return pitchPoints;
	}

	/**
	 * Gets the normalised RGB value of the given pixel
	 * for the last frame processed by processImage()
	 * @param x the X coordinate of the pixel
	 * @param y the Y coordinate of the pixel
	 * @return the normalised RGB colour of the given pixel
	 */
	public Color getNormalisedRgb(int x, int y) {
		return currentRgb[x][y];
	}

	/**
	 * Gets the original (non-normalised) Color of the given pixel
	 * for the last frame processed by processImage()
	 * @param x the X coordinate of the pixel
	 * @param y the Y coordinate of the pixel
	 * @return the normalised RGB colour of the given pixel
	 */
	public Color getRgb(int x, int y) {
		if(currentFrame == null)
			return null;
		return new Color(currentFrame.getRGB(x, y));
	}
	
	/**
	 * Gets the normalised HSB value of the given pixel
	 * for the last frame processed by processImage()
	 * @param x the X coordinate of the pixel
	 * @param y the Y coordinate of the pixel
	 * @return a float array containing the { H, S, B } values for that pixel
	 */
	public float[] getNormalisedHsb(int x, int y) {
		return currentHsb[x][y];
	}
	
	/**
	 * Creates a new world state listener to look for updates in the given world state, 
	 * as often as you tell it to, using some virtual callback you give it
	 * @param targetFps the target FPS
	 * @param state the WorldState to update
	 */
	public WorldStateListener(int targetFps, WorldState state) {
		this.targetFrameTime = 1000 / targetFps;
		this.state = state;
	}
	
	/**
	 * Gets the target FPS
	 */
	public int getTargetFps() {
		return (int)(1000 / targetFrameTime);
	}
	/**
	 * Gets the current FPS
	 */
	public int getCurrentFps() {
		return (int)(1000.0 / currentFrameTime);
	}
	/**
	 * Gets the current world state
	 */
	public WorldState getWorldState() {
		return state;
	}
	
	/**
	 * Gets the current image
	 */
	public BufferedImage getCurrentFrame() {
		return currentFrame;
	}
	/**
	 * Updates the image the thread should work on
	 * @param currentFrame the new image
	 */
	public void setCurrentFrame(BufferedImage currentFrame) {
		this.currentFrame = currentFrame;
	}

	/**
	 * The main function of the listener;
	 * Starts listening for image updates (ignoring the first couple),
	 * and, provided there are boundaries (added using the corresponding methods)
	 * calls the processImage() and updateWorld() functions
	 */
	public void run() {
		
		long startT,
			endT,
			dt;
		
		System.out.println("WorldStateListener: started");
		
		//foreva
		while(true) {
			startT = System.currentTimeMillis();
			
			//if we have a valid, changed image
			if(currentFrame != lastFrame && currentFrame != null) {
				
				//have we done preprocessing?
				if (!preprocessed) {
					preprocessImage(currentFrame);
				}
				else {
					processImage(currentFrame, currentRgb, currentHsb);
					updateWorld(currentRgb, currentHsb);
				}
				lastFrame = currentFrame;

				//calc sleep time (if needed at all)
				endT = System.currentTimeMillis();
				currentFrameTime = Math.max(1, endT - startT);
				dt = targetFrameTime - currentFrameTime;
				if(dt > SLEEP_ACCURACY)
					safeSleep(dt);
			}
			else {
				currentFrameTime = targetFrameTime;
				safeSleep(targetFrameTime);
			}
		}
		
	}
	
	/**
	 * Eats exceptions! And also sleeps
	 * @param ms the timeout, in milliseconds
	 */
	private static void safeSleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) { 
			//got interrupted
			//so what?
			//just keep running while the app is alive!
		}
	}
	
	/**
	 * Does initial preprocessing on the image, executed only once
	 * Calculates game field hull and normalised colours
	 */
	protected void preprocessImage(BufferedImage img) {
		// Ensure the pre-process is only ran once during
		// initialisation; but ignore first few frames (which are always
		// highly distorted)
		if (++keyframe >= FRAME_IGNORE_COUNT && hasBoundary()) {
			
			//get boundary
			Point2 pa = boundaryPoints[0],
				pb = boundaryPoints[1];
			int minX = Math.min(pa.x, pb.x),
				maxX = Math.max(pa.x, pb.x),
				minY = Math.min(pa.y, pb.y),
				maxY = Math.max(pa.y, pb.y);
			

			//get all white pixels
			ArrayList<Point2> whitePoints = new ArrayList<Point2>();
			Color cRgb;
			float[] cHsb;
			for (int x = minX; x < maxX; x++) {
				for (int y = minY; y < maxY; y++) {
					//get colors
					cRgb = new Color(img.getRGB(x, y));
					
					cHsb = currentHsb[x][y];
					Color.RGBtoHSB(cRgb.getRed(), cRgb.getGreen(), cRgb.getBlue(), cHsb);
					
					//save em (used for normalisation calculation)
					currentRgb[x][y] = cRgb;
					
					//add to white if white
					if (Colors.isWhite(cRgb, cHsb)) {
						Point2 p = new Point2(x, y);
						whitePoints.add(p);
					}
				}
			}
			
			//find their hull
			LinkedList<Point2> borders = Alg.convexHull(whitePoints);
			
			pitchPoints.clear();
			for (int x = minX; x < maxX; x++) {
				for (int y = minY; y < maxY; y++) {
					if (Alg.isInHull(borders, x, y)) {
						//finally get all points on the inside
						Point2 p = new Point2(x,y);
						pitchPoints.add(p);

						//but after finding min/max brightness!
						float b = currentHsb[x][y][2];
						if(b < minMaxBrightness[0])	//min
							minMaxBrightness[0] = b;
						if(b > minMaxBrightness[1])	//max
							minMaxBrightness[1] = b;
					}
				}
			}
			preprocessed = true;
			System.out.println("WorldState: preprocessed image, starting capture!");
		}
		
	}


	/**
	 * When overridden in a derived class should process the image, 
	 * normalising and storing all colours in the provided RGB/HSB matrices
	 * Implementation should make use of this.minMaxBrightness
	 * @param img the image to process
	 * @param cRgb a matrix holding the normalised RGB values
	 * @param cHsb a matrix holding the normalised HSB values
	 */
	protected abstract void processImage(BufferedImage img, Color[][] cRgb, float[][][] cHsb);
	

	/**
	 * Overridden in a derived class to support arbitrary updates to some WorldState given a new frame
	 * Implementation should make use of this.state and this.pitchPoints
	 * @param cRgb the RGB values for the new frame
	 * @param cHsb the HSB values for the new frame
	 */
	public abstract void updateWorld(Color[][] cRgb, float[][][] cHsb);

	/**
	 * Gets whether the specified point is in the pitch
	 */
	public boolean pointInPitch(Point2 p) {
		return pitchPoints.contains(p);
	}


	
}
