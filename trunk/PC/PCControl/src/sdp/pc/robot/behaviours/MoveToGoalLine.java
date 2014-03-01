package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

/**
 * Behaviour for sending the robot to the goal mouth.
 */
public class MoveToGoalLine extends RobotBehavior {

	private static final int SAFE_DIST_FROM_GOAL = 30;

	public MoveToGoalLine(Robot robot) { super(robot); }

	/**
	 * Take control when all of the following are true:
	 *  - Defending (this) robot is not near the goal line
	 *  - Ball is in our attacker's or opponent's defender quadrants
	 */
	@Override
	public boolean takeControl() {
		boolean nearTheGoal = robot.getPosition().distance(robot.ourGoalCentre()) <= SAFE_DIST_FROM_GOAL;
		int ballQuadrant = robot.getWorldState().getBallQuadrant();
		int ourAttQuadrant = robot.getWorld().getAttackerQuadrant();
		int oppDefQuadrant;
		if (ourAttQuadrant == 3) {
			oppDefQuadrant = 4;
		} else {
			oppDefQuadrant = 1;
		}
		return !nearTheGoal && (ballQuadrant == ourAttQuadrant || ballQuadrant == oppDefQuadrant);
	}

	@Override
	public void action() {
		while (!suppressed) {
			//TODO Implement this (by referring to Robot.java)
		}
	}
}