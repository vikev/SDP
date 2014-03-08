package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Point2;

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
	 * Take control when all of the following are true:
	 *  - ball is moving with sufficient velocity
	 *  - ball is moving into the robot's direction
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
	 */
	private int myQuadrant() {
		return robot.getWorldState().getDefenderQuadrant();
	}
	

	@Override
	public boolean actionFrame() throws Exception {
		Point2 predBallPos = robot.getWorldState().getFutureData().getEstimate();

		// If that position exists, go to its Y coordinate, otherwise stop.
		if (!predBallPos.equals(Point2.EMPTY)) {
			if (robot.defendToY(predBallPos.getY(), DEFEND_EPSILON_DISTANCE)) {
				robot.getDriver().stop();
				return true;
			}
		} else {
			robot.getDriver().stop();
			return true;
		}
		return false;
	}
}