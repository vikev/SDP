package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.WorldState;

public abstract class RobotBehavior implements Behavior {

	/**
	 * The default timeout duration for blocking operations
	 */
	protected static final int DEFAULT_SLEEP_DURATION = 50;
	
	/**
	 * Whether the behavior's action should be suppressed as soon as possible. 
	 */
	protected boolean suppressed;
	
	/**
	 * The robot instance this behaviour is controlling
	 */
	protected final Robot robot;
	
	/**
	 * Gets the world state this behaviour is operating in
	 */
	protected WorldState getWorldState() {
		return robot.getWorldState();
	}

	/**
	 * Constructs a new <b>RobotBehavior</b> to control a given <b>Robot</b> instance. 
	 * Must be called from a subclass as it is the only constructor. 
	 * @param robot
	 */
	public RobotBehavior(Robot robot) {
		this.robot = robot;
	}

	/**
	 * Instructs the behaviour to release control of the Robot as soon as possible. 
	 */
	@Override
	public final void suppress() {
		suppressed = true;
	}

	/**
	 * Sleeps the current thread with the given timeout
	 */
	protected final void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch(Exception e) {
			System.err.println("An exception occured in RobotBehavior.sleep()");
		}
	}
	
	/**
	 * Sleeps the current thread with the default timeout
	 */
	protected final void sleep() {
		sleep(DEFAULT_SLEEP_DURATION);
	}
	
	@Override
	public void action() {
		while(!suppressed && takeControl()) {
			try {
				if(actionFrame())
					break;
				sleep();
			}
			catch(Exception e) {
				System.out.println("Exception while running " + this.toString());
			}
		}
	}

	/**
	 * Same as default action() but doesn't need to check for suppressed or takeControl().
	 * <p>
	 * All implementing functions should be short (non-blocking)!
	 * @return Whether the behaviour should release control. 
	 */
	public abstract boolean actionFrame() throws Exception;
	
}
