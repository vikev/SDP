package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.Point2;

/**
 * Behaviour which performs the goal of defending the robot. It
 * should only be called when the ball is not moving (or ball is not on the
 * pitch) and the opponent's attacker is on the pitch. It checks the
 * predicted stop location of the imaginary ball if the attacking robot
 * would kick now and moves to predicted position's Y coordinate by going
 * forwards or backwards.
 */
public class DefendRobot extends RobotBehavior {

	private static final double DEFEND_EPSILON_DISTANCE = 8.0;

	public DefendRobot(Robot robot) { super(robot); }
	
	private Point2 predictedPos;

	/**
	 * Take control when:
	 *  - Opponent's attacker(defender) has a ball
	 *  - There exists predicted ball position
	 */
	@Override
	public boolean takeControl() {
		int ourTeam = robot.getTeam();
		int myId = robot.getId();
		int oppTeam = 1 - ourTeam;
		int oppId = myId;	//should be other team's opposite robot
		// Get my location
		Point2 robotPos = robot.getWorldState().getRobotPosition(ourTeam, myId);

		// Get position and facing of the attacker
		Point2 otherPos = robot.getWorldState().getRobotPosition(oppTeam, oppId);
		double otherFacing = robot.getWorldState().getRobotFacing(oppTeam, oppId);

		// Get predicted ball position if that other robot shot just now
		predictedPos = FutureBall.matchingYCoord(otherPos,
					otherFacing, robotPos);
		
		return robot.hasBall(oppTeam, oppId)
				&& !predictedPos.equals(Point2.EMPTY);
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