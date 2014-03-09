package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.Point2;

/**
 * Behaviour which performs (briefly) the goal of defending the
 * ball. It checks the predicted stop location of the ball and moves to its
 * Y coordinate by going forwards or backwards.
 */
public class DefendBall extends RobotBehavior {

	private static final double BALL_SPEED_THRESHOLD = 30.0;
	private static final double DEFEND_EPSILON_DISTANCE = 8.0;

	public DefendBall(Robot robot) { super(robot); }

	/**
	 * Take control when all of the following are true:
	 *  - ball is moving with sufficient velocity
	 *  - ball is not between the robot and our goals
	 */
	@Override
	public boolean takeControl() {
		Point2 myPos = robot.getPosition();
		Point2 ourGoalCentre = robot.ourGoalCentre();
		Point2 ballPos = robot.getWorldState().getBallPosition();
		return robot.getWorldState().getBallSpeed() > BALL_SPEED_THRESHOLD;
				//&& FutureBall.betweenTwoPoints(ballPos.getX(), myPos.getX(), ourGoalCentre.getX());
	}

	@Override
	public boolean actionFrame() throws Exception {
		Point2 predBallPos = robot.getWorldState().getFutureData().getEstimate();

		// If that position exists, go to its Y coordinate, otherwise stop.
		if (!predBallPos.equals(Point2.EMPTY)) {
			if (robot.defendToY(predBallPos.getY(), DEFEND_EPSILON_DISTANCE)) {
				return true;
			}
		} else {
			return true;
		}
		return false;
	}
}