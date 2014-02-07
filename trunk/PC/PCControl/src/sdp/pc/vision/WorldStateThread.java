package sdp.pc.vision;

import java.awt.image.BufferedImage;

public abstract class WorldStateThread extends Thread {

	private static final long SLEEP_ACCURACY = 1;
	
	private BufferedImage currentImage;
	private final WorldState state;
	
	
	private long currentFrameTime;
	private final long targetFrameTime;

	/**
	 * Creates a new world state thread to update the said world state, 
	 * as often as you tell it to, using some virtual callback you give it
	 * @param targetFps the target FPS
	 * @param state the WorldState to update
	 */
	public WorldStateThread(int targetFps, WorldState state) {
		this.setDaemon(true);
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
		return (int)(1000 / currentFrameTime);
	}
	
	/**
	 * Updates the image the refresher should work on
	 * @param currentImage the new image
	 */
	public void setCurrentImage(BufferedImage currentImage) {
		this.currentImage = currentImage;
	}

	/**
	 * Gets the current world state
	 */
	public WorldState getWorldState() {
		return state;
	}
	
	public void run() {
		
		long startT,
			endT,
			dt;
		
		//foreva
		while(true) {
			startT = System.currentTimeMillis();
			
			//update the world
			//changing the image from a different thread while updating is ok
			if(currentImage != null && state != null)
				updateWorld(currentImage, state);
			
			//calc sleep time (if needed at all)
			endT = System.currentTimeMillis();
			currentFrameTime = endT - startT;
			dt = targetFrameTime - currentFrameTime;
			if(dt > SLEEP_ACCURACY)
				try {
					Thread.sleep(dt);
				} catch (InterruptedException e) { 
					//got interrupted
					//so what?
					//just keep running while the app is alive!
				}
			
		}
		
	}
	
	/**
	 * Overriden in a derived class to support arbitrary updates to some WorldState given a new frame
	 * @param img the current frame
	 * @param state the 
	 */
	public abstract void updateWorld(BufferedImage img, WorldState state);

}
