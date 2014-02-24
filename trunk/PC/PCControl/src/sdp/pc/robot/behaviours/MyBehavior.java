package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.WorldState;

public abstract class MyBehavior implements Behavior {

	/**
	 * The default timeout duration for blocking operations
	 */
	protected static final int DEFAULT_SLEEP_DURATION = 50;
	
	protected boolean suppressed;
	protected final Robot robot;
	
	protected WorldState getWorld() {
		return robot.getWorld();
	}

	public MyBehavior(Robot robot) {
		this.robot = robot;
	}

	@Override
	public final void suppress() {
		suppressed = true;
	}
}
