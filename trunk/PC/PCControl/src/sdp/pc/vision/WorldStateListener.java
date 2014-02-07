package sdp.pc.vision;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A generic world state updater. 
 * Assumes that the input image is going to be changed more often than
 * the world state is updated (the latter being costlier)
 * Requires two points
 * @author s1141301
 *
 */
public abstract class WorldStateListener implements Runnable {

	private static final long SLEEP_ACCURACY = 5;
	private final int FRAME_IGNORE_COUNT = 50;
	private final int BOUNDARY_COUNT = 2;

	private BufferedImage lastImage;
	private BufferedImage currentImage;
	
	//color vals
	private Color[][] currentRgb = new Color[700][520];
	private float[][][] currentHsb = new float[700][520][3];
	
	//preprocessing
	private boolean preprocessed = false;
	/**
	 * Keeps track of the frames skipped so far. Should become equal to FRAME_IGNORE_COUNT 
	 * before preprocessing should take place
	 */
	private int keyframe = 0;
	
	//fps measuring
	private long currentFrameTime = 1;	//div by zero!
	private final long targetFrameTime;
	
	//boundary for finer table detection
	private ArrayList<Point2> boundaryPoints = new ArrayList<Point2>();
	
	//protected stuff usable by kidz of the class
	protected final WorldState state;
	protected ArrayList<Point2> pitchPoints = new ArrayList<Point2>();
	
	
	public boolean isPreprocessed() {
		return preprocessed;
	}
	
	public ArrayList<Point2> getPitchPoints() {
		return pitchPoints;
	}
	
	/**
	 * Holds { min-observed-brightness, max-observed-brightness }
	 */
	protected float[] minMaxBrightness = new float[] { 1, 0 };
	
	/**
	 * Adds a boundary point for image clipping
	 * @param p the point to add
	 * @return whether we should add any more points for the boundary to be finished
	 */
	public boolean addBoundary(Point2 p) {
		if(boundaryPoints.size() < BOUNDARY_COUNT)
			boundaryPoints.add(p);
		return hasBoundary();
	}

	public Color getNormalisedRgb(int x, int y) {
		return currentRgb[x][y];
	}
	
	public Color getRgb(int x, int y) {
		if(currentImage == null)
			return null;
		return new Color(currentImage.getRGB(x, y));
	}
	
	public float[] getNormalisedHsb(int x, int y) {
		return currentHsb[x][y];
	}
	
	public boolean hasBoundary() {
		return boundaryPoints.size() == BOUNDARY_COUNT;
	}
	
	/**
	 * Creates a new world state thread to update the said world state, 
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
	public BufferedImage getCurrentImage() {
		return currentImage;
	}
	/**
	 * Updates the image the thread should work on
	 * @param currentImage the new image
	 */
	public void setCurrentImage(BufferedImage currentImage) {
		this.currentImage = currentImage;
	}

	public void run() {
		
		long startT,
			endT,
			dt;
		
		System.out.println("WorldStateListener: started");
		
		//foreva
		while(true) {
			startT = System.currentTimeMillis();
			
			//if we have a valid, changed image
			if(currentImage != lastImage && currentImage != null) {
				
				//have we done preprocessing?
				if (!preprocessed) {
					preprocessImage(currentImage);
				}
				else {
					processImage(currentImage, currentRgb, currentHsb);
					updateWorld(currentRgb, currentHsb);
				}
				lastImage = currentImage;

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
	 * The initial preprocessing on the image, done only once
	 * Calculates game field hull and normalised colours
	 */
	protected void preprocessImage(BufferedImage img) {
		// Ensure the pre-process is only ran once during
		// initialisation; but ignore first few frames (which are always
		// highly distorted)
		if (++keyframe >= FRAME_IGNORE_COUNT && hasBoundary()) {
			
			//get boundary
			Point2 pa = boundaryPoints.get(0),
				pb = boundaryPoints.get(1);
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


	
}
