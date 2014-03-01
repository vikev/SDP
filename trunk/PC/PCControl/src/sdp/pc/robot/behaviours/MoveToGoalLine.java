package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Point2;

/**
 * Behaviour for sending the robot to the goal mouth.
 */
public class MoveToGoalLine extends RobotBehavior {

	private static final int SAFE_DIST_FROM_GOAL = 30;
	
	private static final double EPSILON = 10.0;

	public MoveToGoalLine(Robot robot) { super(robot); }
	
	private Point2 goal_centre = new Point2(0,0);

	/**
	 * Take control when all of the following are true:
	 *  - Defending (this) robot is not near the goal line
	 *  - Ball is in our attacker's or opponent's defender quadrants
	 */
	@Override
	public boolean takeControl() {
		goal_centre = robot.ourGoalCentre();
		boolean nearTheGoal = robot.getPosition().distance(goal_centre) <= SAFE_DIST_FROM_GOAL;
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
	public boolean actionFrame() throws Exception {
		if (robot.goTo(new Point2(goal_centre.getX() + robot.getGoalOffset(), goal_centre.getY()), EPSILON)) {
			return true;
		}
		return false;
	}
}