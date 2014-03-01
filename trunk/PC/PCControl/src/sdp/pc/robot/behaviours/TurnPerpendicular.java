package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Alg;

/**
 * A sample of a behavior which turns its controlled robot towards the perpendicular. 
 * <p>
 * Completely untested!
 * 
 * @author s1141301
 */
public class TurnPerpendicular extends RobotBehavior {
	
	/**
	 * The allowed error from the vertical
	 */
	private static final int ROTATION_THRESHOLD = 10;
	
	/**
	 * One of the vertical lines we wanna face
	 */
	private static final double CORRECT_ROTATION = 90;
	
	//some direction constants
	private static final int RIGHT = 1;
	private static final int LEFT = 0;

	private int direction = 0;
	
	//copy-paste that
	public TurnPerpendicular(Robot robot) { super(robot); }
	
	/**
	 * Take control when all of the following are true:
	 * 	- Ball is not in the robot's quadrant
	 *  - Robot is not asserting perpendicular
	 */
	@Override
	public boolean takeControl() {
		
		double f = robot.getFacing();
		
		//turn angle to upper/lower perpendicular
		double dUp = Alg.normalizeToBiDirection(Math.abs(f - CORRECT_ROTATION - 180));
		double dDown = Alg.normalizeToBiDirection(Math.abs(f - CORRECT_ROTATION));
		
		//the lower turn angle
		double d = Math.min(dUp, dDown);
		
		//the direction to turn to; xor magic
		direction = ((f > 90 || f < -90) ^ (dUp > dDown)) ? RIGHT : LEFT;
		
		return d < ROTATION_THRESHOLD;
	}

	@Override
	public void action() {
		try {
			
			while(takeControl() && !suppressed) {
				if(direction == LEFT)
					robot.getDriver().turnLeft();
				else
					robot.getDriver().turnRight();
				Thread.sleep(DEFAULT_SLEEP_DURATION);
			}
			
			robot.getDriver().stop(); 
		}
		catch (Exception e) {
			//TODO: how do we handle such exceptions?
			// - hope it never happens...
		}
	}
}
