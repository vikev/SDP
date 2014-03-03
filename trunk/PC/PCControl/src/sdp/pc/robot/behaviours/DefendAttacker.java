package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.Point2;

/**
 * TO BE USED ONLY FOR THE DEFENDING ROBOT
 * Behaviour which performs the goal of defending the robot. It
 * should only be called when the ball is not moving (or ball is not on the
 * pitch) and the opponent's attacker is on the pitch. It checks the
 * predicted stop location of the imaginary ball if the attacking robot
 * would kick now and moves to predicted position's Y coordinate by going
 * forwards or backwards.
 */
public class DefendAttacker extends RobotBehavior {

	private static final double BALL_SPEED_THRESHOLD = 30.0;
	private static final double DEFEND_EPSILON_DISTANCE = 8.0;

	public DefendAttacker(Robot robot) { super(robot); }
	
	private Point2 predictedPos;

	/**
	 * Take control when:
	 *  - Ball is moving with insufficient velocity
	 *  - Ball is in opponents attacker quadrant 
	 *  - There exists predicted ball position
	 */
	@Override
	public boolean takeControl() {
		int ourTeam = robot.getTeam();
		int myId = robot.getId();
		// Get my location
		Point2 robotPos = robot.getWorldState().getRobotPosition(ourTeam, myId);

		// Get position and facing of the attacker
		Point2 otherPos = robot.getWorldState().getRobotPosition(1 - ourTeam, 1 - myId);
		double otherFacing = robot.getWorldState().getRobotFacing(1 - ourTeam, 1 - myId);

		// Get predicted ball position if that other robot shot just now
		predictedPos = FutureBall.matchingYCoord(otherPos,
					otherFacing, robotPos);
		
		return robot.getWorldState().getBallSpeed() < BALL_SPEED_THRESHOLD 
				&& robot.getWorldState().getBallQuadrant() == ((myQuadrant()  == 1) ? 2 : 3)
				&& !predictedPos.equals(Point2.EMPTY);
		}
	
	/**
	 * Gets the quadrant we are supposed to be in
	 */
	private int myQuadrant() {
		return robot.getWorldState().getDefenderQuadrant();
	}
	
	@Override
	public boolean actionFrame() throws Exception {
		if (robot.defendToY(predictedPos.getY(), DEFEND_EPSILON_DISTANCE)) {
			robot.stop();
			return true;
		} else {
			return false;
		}
	}
}