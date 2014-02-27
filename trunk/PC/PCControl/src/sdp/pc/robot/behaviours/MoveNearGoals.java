package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

/**
 * Behaviour for sending the robot to the goal mouth.
 */
public class MoveNearGoals extends MyBehavior {

	public MoveNearGoals(Robot robot) { super(robot); }

	/**
	 * Take control when the defending robot is not near the goal line
	 */
	@Override
	public boolean takeControl() {
		return false;
	}

	@Override
	public void action() {
		while (!suppressed) {
			//TODO Implement this (by referring to Robot.java)
		}
	}
}