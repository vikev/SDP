package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

/**
 * Behaviour for sending the robot to the goal mouth.
 */
public class MoveToGoalLine extends RobotBehavior {

	public MoveToGoalLine(Robot robot) { super(robot); }

	/**
	 * Take control when the defending robot is not near the goal line
	 */
	@Override
	public boolean takeControl() {
		return true;
	}

	@Override
	public void action() {
		while (!suppressed) {
			//TODO Implement this (by referring to Robot.java)
		}
	}
}