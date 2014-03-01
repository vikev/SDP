package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

/**
 * Behaviour for sending the robot to the goal mouth.
 */
public class MoveToGoalLine extends RobotBehavior {

	public MoveToGoalLine(Robot robot) { super(robot); }

	/**
	 * Take control when all of the following are true:
	 *  - Defending (this) robot is not near the goal line
	 *  - Ball is in our attacker's or opponent's defender quadrants
	 */
	@Override
	public boolean takeControl() {
		return true;
	}
	
	@Override
	public boolean actionFrame() throws Exception {
		// TODO Auto-generated method stub
		//TODO Implement this (by referring to Robot.java)
		return false;
	}
}