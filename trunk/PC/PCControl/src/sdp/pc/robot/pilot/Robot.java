package sdp.pc.robot.pilot;

import static sdp.pc.common.Constants.*;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;
import sdp.pc.vision.Alg;

import static sdp.pc.vision.Alg.*;

/**
 * An abstract class for conducting non-trivial orders on a Robot. A delegator
 * for a Driver. In theory, we could re-write M3att and M3def to use this
 * instead.
 * 
 * Only methods useful from a strategic standpoint should be public.
 * 
 * @author s1143704
 */
public class Robot {

	/**
	 * The desired offset from the goal centre for the robot to initialise
	 * itself to, in pixels. Use a negative value if defending the right-hand
	 * goal.
	 * 
	 * Use getGoalOffset() to get a negative version if we're defending the
	 * right goal.
	 */
	private static final int GOAL_OFFSET = 20;

	/**
	 * How far, at most, from the goalmouth centre the defending robot should
	 * travel before returning, in pixels, while not controlling the ball.
	 */
	private static final double SAFE_DIST_FROM_GOAL = 30;

	/**
	 * How far from a point to be, at most, for the robot to be considered
	 * defending the predicted ball target, in pixels
	 */
	private static final double DEFEND_EPSILON_DISTANCE = 8.0;

	/**
	 * The size of the extra buffer a defender can travel beyond the goalmouth,
	 * in pixels.
	 */
	private static final int BETWEEN_GOALS_EPSILON = 3;

	/**
	 * If an epsilon value doesn't make sense to know in some context, this is a
	 * safe angle value to use
	 */
	private static final double SAFE_ANGLE_EPSILON = 10.0;

	/**
	 * The safe distance in pixels from which the attacker should approach the
	 * ball.
	 */
	private static final int SAFE_APPROACH_DIST = 8;

	/**
	 * The maximum distance in pixels the ball can be away from the centre of
	 * the table. This value is more adjustable for calibration than a precise
	 * number. A higher value will reduce the scaling of projections.
	 */
	private static final double DISTORTION_DISTANCE_MAX = 600.0;

	/**
	 * A safe distance in pixels to use as a window size when a more calibrated
	 * epsilon value isn't available in some context
	 */
	private static final double SAFE_DIST_EPSILON = 10.0;

	/**
	 * The primitive driver used to control the NXT
	 */
	private Driver driver;

	/**
	 * The worldState <b>this</b> should get data from
	 */
	private WorldState state;

	/**
	 * An integer which represents the team of <b>this</b>
	 */
	private int myTeam;

	/**
	 * An integer which represents <b>this</> with respect to myTeam.
	 */
	private int myIdentifier;

	@SuppressWarnings("unused")
	private int myState = State.UNKNOWN;

	/**
	 * Class for controlling a robot from a more abstract point of view
	 * 
	 * @param driver
	 *            - the Driver used to conduct primitive orders on the NXT brick
	 * @param state
	 *            - the WorldState <b>this</b> should get data from
	 * 
	 * @param myTeam
	 *            - the integer identifier which refers to this Robot's team -
	 *            (0, 1) are (yellow, blue) respectively
	 * @param myId
	 *            - the integer identifier which uniquely identifies one of the
	 *            4 robots, with the help of myTeam
	 */
	public Robot(Driver driver, WorldState state, int myTeam, int myId) {
		this.driver = driver;
		this.state = state;
		this.myTeam = myTeam;
		this.myIdentifier = myId;
	}

	/**
	 * Constructor which builds a Driver from a robot id
	 * 
	 * @param robotCode
	 *            - use ChooseRobot.x
	 * @param state
	 *            - the worldstate the robot exists in
	 * @param myTeam
	 *            - team (0, 1) for (yellow, blue) respectively
	 * @param myId
	 *            - the id for the robot (0, or 1)
	 * @throws Exception
	 */
	public Robot(int robotCode, WorldState state, int myTeam, int myId)
			throws Exception {
		TCPClient conn = new TCPClient(robotCode);
		Driver drv = new Driver(conn);
		this.driver = drv;
		this.state = state;
		this.myTeam = myTeam;
		this.myIdentifier = myId;
	}

	public void stop() throws Exception {
		driver.stop();
	}

	public void closeConnection() {
		driver.closeConnection();
	}

	/**
	 * Synchronous method which performs (briefly) the goal of defending the
	 * ball. It checks the predicted stop location of the ball and moves to its
	 * Y coordinate by going forwards or backwards.
	 */
	public void defendBall() throws Exception {

		// Get predicted ball stop point
		Point2 predBallPos = state.getFutureData().getDeflection();

		// If that position exists, go to its Y coordinate, otherwise stop.
		if (!predBallPos.equals(Point2.EMPTY)) {
			defendToY(predBallPos.getY(), DEFEND_EPSILON_DISTANCE);
		} else {
			driver.stop();
		}
	}

	/**
	 * Synchronous method which performs the goal of defending the robot.
	 * It should only be called when the ball is not moving (or ball is
	 * not on the pitch) and the opponent's attacker is on the pitch.
	 * It checks the predicted stop location of the imaginary ball if
	 * the attacking robot would kick now and moves to predicted position's
	 * Y coordinate by going forwards or backwards.
	 */
	public void defendRobot() throws Exception {
		//Get defending robot's position
		Point2 pos = state.getRobotPosition(this.myTeam, this.myIdentifier);
		
		//Get attacker's position and facing
		Point2 attPos = state.getRobotPosition(1 - this.myTeam, 1 - this.myIdentifier);
		double attFacing = state.getRobotFacing(1 - this.myTeam,  1 - this.myIdentifier);

		//Get predicted ball position if the attacker shot now
		Point2 predBallPos = FutureBall
				.estimateMatchingYCoord(attPos, attFacing, pos);
		// Move robot to this position
		if (!predBallPos.equals(Point2.EMPTY)) {
			defendToY(predBallPos.getY(), DEFEND_EPSILON_DISTANCE);
		} else {
			driver.stop();
		}
	}

	/**
	 * Synchronous method which must be called continuously which makes a robot
	 * face a specific angle in degrees. The robot will rotate the direction
	 * which gets their faster.
	 * 
	 * @return true if the robot is already facing deg within a window of
	 *         epsilon
	 * @throws Exception
	 */
	public boolean assertFacing(double deg, double epsilon) throws Exception {
		double rotateBy = normalizeToBiDirection(state.getRobotFacing(myTeam,
				myIdentifier) - deg);
		double speed = getRotateSpeed(rotateBy, epsilon);
		if (rotateBy > epsilon && speed > 1.0) {
			driver.turnLeft(speed);
		} else if (rotateBy < -epsilon && speed > 1.0) {
			driver.turnRight(speed);
		} else {
			return true;
		}
		return false;
	}

	/**
	 * Checks if robot can actually make turn without hitting wall, entering
	 * goal mouth or going through center line. 5 pixels is an estimate from
	 * testing in Milestone 1 and 3
	 */
	public boolean canTurn() {
		return (Math.abs(state.getRobotPosition(myTeam, myIdentifier).getY()
				- state.getPitch().getYBegin()) < 5
				&& Math.abs(state.getRobotPosition(myTeam, myIdentifier).getY()
						- state.getPitch().getYEnd()) < 5
				&& Math.abs(state.getRobotPosition(myTeam, myIdentifier).getX()
						- state.getPitch().getXBegin()) < 5 && Math.abs(state
				.getRobotPosition(myTeam, myIdentifier).getX()
				- state.getPitch().getXEnd()) < 5);
	}

	/**
	 * Makes the robot turn to a point synchronously. Returns true when it is
	 * complete.
	 * 
	 * @throws Exception
	 */
	public boolean turnTo(Point2 to, double eps) throws Exception {
		double ang = normalizeToUnitDegrees(state.getRobotPosition(myTeam,
				myIdentifier).angleTo(to));
		if (assertFacing(ang, eps)) {
			return true;
		}
		return false;
	}

	/**
	 * Makes the robot turn to the point then move forward to the point (returns
	 * true if complete)
	 */
	public boolean goTo(Point2 to, double eps) throws Exception {
		if (turnTo(to, SAFE_ANGLE_EPSILON)) {
			if (moveForwardTo(to, eps)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates distance to opposite team's goal.
	 */
	public double getDistanceToOppositeGoal() {
		if (state.getDirection() == 0)
			return state.getRobotPosition(myTeam, myIdentifier).distance(
					state.getLeftGoalCentre());
		return state.getRobotPosition(myTeam, myIdentifier).distance(
				state.getRightGoalCentre());
	}

	/**
	 * Method for sending the robot to the goalmouth. Must be called
	 * continuously because it is synchronous.
	 */
	public boolean assertNearGoalLine(double eps) {
		try {
			Point2 botPos = state.getRobotPosition(myTeam, myIdentifier);
			Point2 goal_centre;
			if (myIdentifier == 0) {
				goal_centre = state.getLeftGoalCentre();
			} else {
				goal_centre = state.getRightGoalCentre();
			}
			if (botPos.distance(goal_centre) > SAFE_DIST_FROM_GOAL) {
				if (goTo(new Point2(goal_centre.getX() + getGoalOffset(),
						goal_centre.getY()), eps)) {
					return true;
				}
			} else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Method for telling the robot to face a perpendicular angle. Has to be
	 * called continuously, but will return true when it's done
	 * 
	 * @param state
	 * @param vision
	 * @param driver
	 * @return
	 * @throws Exception
	 */
	public boolean assertPerpendicular(double eps) throws Exception {

		// Calculate which angle is the closest perpendicular one
		double target;
		double face = state.getRobotFacing(myTeam, myIdentifier);

		double a = normalizeToBiDirection(face - 90.0);

		if (Math.abs(a) < 90.0) {
			target = 90.0;
		} else {
			target = 270.0;
		}

		// Do it
		if (assertFacing(target, eps)) {
			driver.stop();
			return true;
		}
		return false;
	}

	/**
	 * WIP Attempts to kick the ball by first navigating to a point
	 * (apprachPoint) from which it can then approach the ball and kick it
	 * towards the target goal.
	 * 
	 * <ul>
	 * <li>Navigate the robot around the ball when the ball obstructs the direct
	 * path to the approach point.</li>
	 * <li>Look into how to behave if the ball is too close to the white
	 * boundary to get behind it to take a shot at the goal. -> Calculate an
	 * angle to bounce off the wall in this case, and adjust your approach angle
	 * accordingly -Blake</li>
	 * <li>Create meaningful exception handling code</li>
	 * </ul>
	 * 
	 * TODO: Should be checked to conform to defender standards for modularity.
	 * @throws Exception 
	 **/
	public boolean approachStationaryBall() throws Exception {

		// Get the robot, ball, and targets as Point2
		Point2 robotPosition = state.getRobotPosition(this.myTeam, this.myIdentifier);
		Point2 ballPosition = state.getBallPosition();
		double angleBetween = calculateAngleBetween(robotPosition, ballPosition);

		// check if the ball will obstruct a direct path to the approach
		// point move to a point where a direct path can be taken to the
		// approach point
		Point2 approachPoint = calculateApproachPoint(ballPosition.invertY(),
				getBallTarget().invertY(), state.getDirection());
		if (approachPoint.equals(Point2.EMPTY)) {
			System.out.println("Could not assign Approach Point");
			return false;
		}
		
		// If angleBetween is large, the robot is nearly in line with the goal
		// already (small angle means the robot is between the ball and goal)
		if ((angleBetween > 90.0)) {

			// Attempt to navigate to the approach point and then kick it.
			if (!goTo(approachPoint, SAFE_DIST_EPSILON)) {
				return false;
			}
		} else {

			// Checks if robot is between the ball and the goal
			// If it is, move up or down
			// *Need to also account for the ball's radius - Robaidh*
			if (Math.abs(robotPosition.getY() - ballPosition.getY()) < ATTACKER_LENGTH) {
				int newY;
				if (robotPosition.getY() > ballPosition.getY()) {
					newY = robotPosition.getY() + ATTACKER_LENGTH + SAFE_APPROACH_DIST;
				} else {
					newY = robotPosition.getY() - ATTACKER_LENGTH - SAFE_APPROACH_DIST;
				}
				if (!goTo(new Point2(robotPosition.getX(), newY), SAFE_DIST_EPSILON)) {
					return false;
				}
			}

			// Moves horizontally until it aligns with the Approach Point
			Point2 intermediatePoint = new Point2(approachPoint.getX(),
					robotPosition.getY());

			if (goTo(intermediatePoint, SAFE_DIST_EPSILON)) {
				if (!goTo(approachPoint, SAFE_DIST_EPSILON)) {
					return false;
				}
			} else {
				return false;
			}
		}
		//Finally, kick the ball
		if (kickStationaryBall()) {
			return true;
		}
		return false;
	}

	/**
	 * Calculates angle between robot, ball and ball target positions
	 * @param robotPosition
	 * @param ballPosition
	 * @return angle between given values
	 */
	private double calculateAngleBetween(Point2 robotPosition, Point2 ballPosition) {
		Point2 targetPoint = getBallTarget();

		// Get y-inverted versions
		Point2 robotPositionInvertedY = robotPosition.invertY();
		Point2 ballPositionInvertedY = ballPosition.invertY();
		Point2 targetPointInvertedY = targetPoint.invertY();

		// Y coordinates given must be already inverted
		Point2 vectorBalltoTargetPoint = new Point2(ballPosition.getX()
				- targetPoint.getX(), ballPositionInvertedY.getY()
				- targetPointInvertedY.getY());
		Point2 vectorBalltoSelectedRobot = new Point2(ballPosition.getX()
				- robotPosition.getX(), ballPositionInvertedY.getY()
				- robotPositionInvertedY.getY());
		double angleBetween = calculateAngle(vectorBalltoTargetPoint,
				vectorBalltoSelectedRobot);

		return angleBetween;
	}
	/**
	 * Kicks stationary ball. Assumes robot is already at the approach point.
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public boolean kickStationaryBall() throws InterruptedException, Exception {
		Point2 ballPosition = state.getBallPosition();

		// Turn towards ball
		if (turnTo(ballPosition, SAFE_ANGLE_EPSILON)) {
			driver.stop();
			Thread.sleep(100);
			driver.stop();
			// Move slightly forward and kick
			driver.forward(80);
			Thread.sleep(1000);
			driver.kick(5000);
			return true;
		}
		return false;
	}

	/**
	 * Makes the robot, which should already be perpendicular, move forward or
	 * backward to cut off the estimated ball postion's Y coordinate. The method
	 * is synchronous, and therefore must be called until it returns true if you
	 * expect it to be finished.
	 */
	private boolean defendToY(int y, double eps) throws Exception {

		double botFacing = state.getRobotFacing(myTeam, myIdentifier);
		Point2 botPosition = state.getRobotPosition(myTeam, myIdentifier);
		int botX = botPosition.getX();
		double angleToBall = botPosition.angleTo(new Point2(botX, y));
		boolean between = Alg.pointBetweenGoals(new Point2(y), myIdentifier,
				BETWEEN_GOALS_EPSILON);
		int estStopY = state.getFutureData().getResult().getY();

		// Compare robot facing with angle to ball
		double diff = normalizeToBiDirection(botFacing - angleToBall);

		// If the robot is far enough from the target, and between the goals:
		if (botPosition.distance(new Point2(botX, estStopY)) > eps && between) {

			// Assert the robot is near the target, by going forward or backward
			if (Math.abs(diff) > 90) {
				moveBackwardTo(new Point2(botX, estStopY), eps);
			} else {
				moveForwardTo(new Point2(botX, estStopY), eps);
			}
		} else {
			driver.stop();
			return true;
		}
		return false;
	}

	/**
	 * Returns the desired rotate speed based on how far the robot has to rotate
	 * before it finishes. Useful because there is a delay associated with
	 * frames of world state, and we can't rotate at full-speed all the time.
	 * 
	 * @return TODO: speed in motor-degrees (?) per second
	 */
	private static double getRotateSpeed(double rotateBy, double epsilon) {
		rotateBy = Math.abs(rotateBy);
		if (rotateBy > 75.0) {
			return 100.0;
		} else if (rotateBy > 25.0) {
			return 35.0;
		} else if (rotateBy > epsilon) {
			return 15.0;
		} else {
			return 0.0;
		}
	}

	/**
	 * Makes the robot go forward as long as it's outwith <b>to</b> with
	 * windowsize epsilon. We therefore assume the robot is facing the target
	 * point Synchronous and must be called continuously.
	 */
	private boolean moveForwardTo(Point2 to, double epsilon) throws Exception {
		Point2 robLoc = state.getRobotPosition(myTeam, myIdentifier);
		if (robLoc.distance(to) < epsilon) {
			return true;
		}
		double speed = getMoveSpeed(robLoc.distance(to),
				DEFEND_EPSILON_DISTANCE);
		driver.forward(speed);
		return false;
	}

	/**
	 * Makes the robot go backward as long as it's outwith <b>to</b> with
	 * windowsize epsilon. We therefore assume the robot is perpendicular.
	 * Synchronous and must be called continuously.
	 */
	private boolean moveBackwardTo(Point2 to, double epsilon) throws Exception {
		Point2 robLoc = state.getRobotPosition(myTeam, myIdentifier);
		if (robLoc.distance(to) < epsilon) {
			return true;
		}
		double speed = getMoveSpeed(robLoc.distance(to),
				DEFEND_EPSILON_DISTANCE);
		driver.backward(speed);
		return false;
	}

	/**
	 * Similar to getRotateSpeed, gets a reasonable move speed for the robot
	 * depending how far it is from the target.
	 * 
	 * TODO: Units? motor velocity in radians per second or..?
	 */
	private static double getMoveSpeed(double distance, double eps) {
		if (distance > 60.0) {
			return 300.0;
		} else if (distance > 25.0) {
			return 140.0;
		} else if (distance > eps) {
			return 30.0;
		} else {
			return 1.0;
		}
	}

	/**
	 * Returns how far from the goal to go (just GOAL_OFFSET times -1 on the
	 * right side)
	 * 
	 * @return
	 */
	private int getGoalOffset() {
		return (int) (Math.pow(-1, state.getDirection() + 1) * GOAL_OFFSET);
	}

	/**
	 * Gets a point we desire to kick towards. Currently only returns the centre
	 * of the target goal.
	 * 
	 * @return
	 */
	private Point2 getBallTarget() {
		int dir = state.getDirection();

		// If the target goal is the right-side one
		if (dir == DIRECTION_RIGHT)
			return state.getRightGoalCentre();
		if (dir == DIRECTION_LEFT)
			return state.getLeftGoalCentre();
		return Point2.EMPTY;
	}

	/**
	 * Calculates the (smallest) angle between two vectors represented by
	 * Point2s, in degrees
	 * 
	 * TODO: This method has no business being static inside Robot.
	 * calculateAngle performs some maths which can be accomplished
	 * (differently) by existing code. Either update kickStationaryBall to
	 * behave like the defender code (comparing angles) or move this to Alg (?)
	 * 
	 * @param vectorA
	 * @param vectorB
	 * @return
	 */
	private static double calculateAngle(Point2 vecA, Point2 vecB) {
		double vecAMagnitude = vecA.modulus();
		double vecBMagnitude = vecB.modulus();

		int dotProduct = vecA.dot(vecB);

		double ang = Math.acos(dotProduct / (vecAMagnitude * vecBMagnitude));

		return Math.toDegrees(ang);
	}

	/**
	 * Calculates the attacking robots approach point to the ball (auxiliary
	 * method). Assumes that Y coordinates have already been inverted.
	 * 
	 * @param ballPosition
	 * @param ballTargetPoint
	 * @param targetGoal
	 * @return
	 */
	private Point2 calculateApproachPoint(Point2 ballPosition,
			Point2 ballTargetPoint, int targetGoal) {

		// Initialise an empty point
		Point2 approachPoint = Point2.EMPTY.copy();

		// Calculate distortion scale. Bigger value = further away from the ball
		double distortionScale = 1.0
				+ Vision.getCameraCentre().distance(ballPosition)
				/ DISTORTION_DISTANCE_MAX;

		// TODO: Where did this 50.0 come from?
		double gradBallToGoal = ballPosition.gradTo(ballTargetPoint) * 50.0;

		// Use -1^z to calculate if left or right desired
		double pow = Math.pow(-1, targetGoal);

		double attackerSize = new Point2(ATTACKER_LENGTH, ATTACKER_WIDTH)
				.modulus();
		approachPoint.setX((int) (ballPosition.getX() + pow * attackerSize
				* distortionScale));
		approachPoint.setY((int) ((gradBallToGoal) + ballPosition.getY()));

		// Y coordinate must be inverted to be positive

		// TODO: Remove all instances of y inversion (it doesn't change anything
		// and is only for keeping points in Quadrant I
		return approachPoint.invertY();
	}

	/**
	 * Static class for referencing robot states once we start making the AI
	 * decision model. Should probably be an Enum but I can't be bothered.
	 * 
	 * @author s1143704
	 */
	private static class State {
		public static int UNKNOWN = 0;
	}
}
