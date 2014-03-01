package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

/**
 * Behaviour which performs (briefly) the goal of defending the
 * ball. It checks the predicted stop location of the ball and moves to its
 * Y coordinate by going forwards or backwards.
 * TODO: Currently works only for defender
 */
public class DefendBall extends RobotBehavior {

	private static final double BALL_SPEED_THRESHOLD = 30.0;
	private static final double DEFEND_EPSILON_DISTANCE = 8.0;

	public DefendBall(Robot robot) { super(robot); }

	/**
	 * Take control when the ball is moving with sufficient velocity
	 * and the ball is not initially in our defenders zone
	 */
	@Override
	public boolean takeControl() {
		// it should rather check whether end position is sufficiently close to the goal line
		// no matter speed/quadrant
		return robot.getWorldState().getBallSpeed() > BALL_SPEED_THRESHOLD 
				&& robot.getWorldState().getBallQuadrant() != myQuadrant();
	}

	/**
	 * Gets the quadrant we are supposed to be in
	 * TODO: check whether the robot is attacker/defender
	 */
	private int myQuadrant() {
		return robot.getWorldState().getDefenderQuadrant();
	}

	@Override
	public void action() {
		while (!suppressed) {
			//check predicted ball position
			//see if we are close enough
			//move?
			//when to end?
		}
	}
}