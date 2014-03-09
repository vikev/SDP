package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Alg;

/**
 * A sample of a behavior which turns its controlled robot towards the perpendicular. 
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
	
	private static int direction = 0;
	
	//copy-paste that
	public TurnPerpendicular(Robot robot) { super(robot); }
	
	/**
	 * Always take control when possible, since this is the
	 * least priority having behaviour.
	 */
	@Override
	public boolean takeControl() {
		double f = robot.getFacing();
		
		//turn angle to upper/lower perpendicular
		double dUp = Alg.normalizeToBiDirection(Math.abs(f - CORRECT_ROTATION - 180));
		double dDown = Alg.normalizeToBiDirection(Math.abs(f - CORRECT_ROTATION));
		
		//the lower turn angle
		double d = Math.min(Math.abs(dUp), Math.abs(dDown));
		
		//the direction to turn to; xor magic
		direction = ((f > 90 || f < -90) ^ (dUp > dDown)) ? RIGHT : LEFT;
		return d > ROTATION_THRESHOLD;
	}

	@Override
	public boolean actionFrame() throws Exception {
		
		if (direction == LEFT)
			robot.getDriver().turnLeft(25);
		else
			robot.getDriver().turnRight(25);
		
		return false;
	}
}
