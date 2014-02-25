package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

/**
 * Behaviour which performs (briefly) the goal of defending the
 * ball. It checks the predicted stop location of the ball and moves to its
 * Y coordinate by going forwards or backwards.
 * TODO Currently works only for defender
 */
public class DefendBall extends MyBehavior {

	private static final double BALL_SPEED_THRESHOLD = 30.0;
	private static final double DEFEND_EPSILON_DISTANCE = 8.0;

	public DefendBall(Robot robot) { super(robot); }

	/**
	 * Take control when the ball is moving with sufficient velocity
	 * and the ball is not initially in our defenders zone
	 */
	@Override
	public boolean takeControl() {
		return robot.getWorld().getBallSpeed() > BALL_SPEED_THRESHOLD && robot.getWorld().getBallQuadrant() != myQuadrant();
	}

	private int myQuadrant() {
		int myDi = robot.getWorld().getDirection();
		int ourDefQ = (myDi == 0) ? 4 : 1;
		return ourDefQ;
	}

	@Override
	public void action() {
		while (!suppressed) {
			//TODO Implement this (by referring to Robot.java)
		}
	}
}