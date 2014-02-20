package sdp.pc.robot.pilot;

import sdp.pc.common.Constants;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.Point2;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;

import static sdp.pc.vision.Alg.*;

/**
 * An abstract class for conducting non-trivial orders on a Robot. A delegator
 * for a Driver. In theory, we could re-write M3att and M3def to use this
 * instead.
 * 
 * @author s1143704
 */
public class Robot {

	/**
	 * The desired offset from the goal centre for the robot to initialise
	 * itself to, in pixels. Use a negative value if defending the right-hand
	 * goal.
	 * 
	 * TODO: The negative change should be automatic if myTeam and/or
	 * myIdentifier change
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
	 * Used by attacker code TODO: better docu
	 */
	private static final int SAFE_APPROACH_DIST = 8;

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

	/**
	 * Class for controlling a robot from a more abstract point of view
	 * 
	 * @param driver
	 *            - the Driver used to conduct primitive orders on the NXT brick
	 * @param state
	 *            - the WorldState <b>this</b> should get data from
	 * 
	 * @param myTeam
	 *            - the integer identifier which refers to this Robot's team.
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
	 * Synchronous method which performs (briefly) the goal of defending the
	 * ball. It checks the predicted stop location of the ball and moves to its
	 * Y coordinate by going forwards or backwards.
	 */
	public void defendBall() throws Exception {

		// Get predicted ball stop point
		Point2 predBallPos = state.getEstimatedStopPoint();

		// If that position exists, go to its Y coordinate, otherwise stop.
		if (!predBallPos.equals(Point2.EMPTY)) {
			defendTo(predBallPos.getY(), DEFEND_EPSILON_DISTANCE);
		} else {
			driver.stop();
		}
	}

	/**
	 * In theory this method would be used for defending against the attacker
	 * while the ball isn't moving, by estimating the robots facing angle and
	 * cutting it off. In practice, we never used this method (and its
	 * implementation does nothing like what's documented here)
	 * 
	 * TODO: Defending against a robot is useful, and we should implement,
	 * abstract, and modularise this.
	 */
	public void defendRobot(Point2 position, double facing) throws Exception {

		// Add some huge velocity
		int x = 200;
		int y = 200;
		if (facing < 180) {
			y = -y;
		}
		if (facing > 270 || facing < 90) {
			x = -x;
		}

		Point2 predBallPos = FutureBall.estimateStopPoint(new Point2(x, y),
				position);
		// Move robot to this position
		if (!predBallPos.equals(Point2.EMPTY)) {
			defendTo(predBallPos.getY(), DEFEND_EPSILON_DISTANCE);
		} else {
			driver.stop();
		}
	}

	/**
	 * Returns true if a given Y coordinate is between the specified goalmouth
	 * endpoints, with some epsilon value.
	 * 
	 * TODO: This method has no business being static while inside Robot.
	 */
	private static boolean betweenGoals(int y, int side, int eps) {
		if (side == 0) {
			return (y + eps < WorldState.leftGoalBottom.getY() && y - eps > WorldState.leftGoalTop
					.getY());
		} else {
			return (y + eps < WorldState.rightGoalBottom.getY() && y - eps > WorldState.rightGoalTop
					.getY());
		}
	}

	/**
	 * Makes the robot, which should already be perpendicular, move forward or
	 * backward to cut off the estimated ball postion's Y coordinate. The method
	 * is synchronous, and therefore must be called until it returns true if you
	 * expect it to be finished.
	 */
	private boolean defendTo(int y, double eps) throws Exception {

		double botFacing = state.getRobotFacing(myTeam, myIdentifier);
		Point2 botPosition = state.getRobotPosition(myTeam, myIdentifier);
		int botX = botPosition.getX();
		double angleToBall = botPosition.angleTo(new Point2(botX, y));
		boolean between = betweenGoals(y, myIdentifier, BETWEEN_GOALS_EPSILON);
		int estStopY = state.getEstimatedStopPoint().getY();

		// Compare robot facing with angle to ball
		double diff = normalizeToBiDirection(botFacing - angleToBall);

		// If the robot is far enough from the target, and between the goals:
		if (botPosition.distance(new Point2(botX, estStopY)) > eps && between) {

			// Assert the robot is near the target, by going forward or backward
			if (Math.abs(diff) > 90) {
				assertNearReverse(new Point2(botX, estStopY), eps);
			} else {
				assertNearForward(new Point2(botX, estStopY), eps);
			}
		} else {
			driver.stop();
			return true;
		}
		return false;
	}

	/**
	 * In theory, should make the robot move somewhere half-way between the
	 * estimated ball position's Y coordinate and the goalmouth's centre, useful
	 * when the ball is not moving and the attacking robot is not wearing a hat.
	 * We never used it for the milestone and therefore it is disabled. We're
	 * suppressing all warnings because comparing DEF_ROBOT to 0 apparently
	 * makes dead code etc.
	 * 
	 * In practice, this method won't really be useful for matches because all
	 * robots will be wearing hats, but the concept behind weighting the goal
	 * centre is useful.
	 */
	@SuppressWarnings("all")
	public void defendIfNoAttacker() throws Exception {
		Point2 robotPosition = state.getRobotPosition(myTeam, myIdentifier);
		Point2 ballPosition = state.getBallPosition();

		double q;
		if (myIdentifier == 0) {
			q = state.getLeftGoalCentre().getY();
		} else {
			q = state.getRightGoalCentre().getY();
		}

		q += ballPosition.getY();
		q /= 2;

		// Move robot to this position
		defendTo((int) q, DEFEND_EPSILON_DISTANCE);
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
	 * Synchronous method which must be called continuously which makes a robot
	 * face a specific angle in degrees. The robot will rotate the direction
	 * which gets their faster.
	 * 
	 * @return true if the robot is already facing deg within a window of
	 *         epsilon
	 */
	public boolean assertFacing(double deg, double epsilon) {
		double rotateBy = normalizeToBiDirection(state.getRobotFacing(myTeam,
				myIdentifier) - deg);
		try {
			double speed = getRotateSpeed(rotateBy, epsilon);
			if (rotateBy > epsilon && speed > 1.0) {
				driver.turnLeft(speed);
			} else if (rotateBy < -epsilon && speed > 1.0) {
				driver.turnRight(speed);
			} else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Makes the robot go forward as long as it's outwith <b>to</b> with
	 * windowsize epsilon. We therefore assume the robot is perpendicular.
	 * Synchronous and must be called continuously.
	 */
	public boolean assertNearForward(Point2 to, double epsilon)
			throws Exception {
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
	public boolean assertNearReverse(Point2 to, double epsilon)
			throws Exception {
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
	 * Makes the robot turn to a point synchronously. Returns true when it is
	 * complete.
	 */
	public boolean turnTo(Point2 to, double eps) {
		double ang = normalizeToUnitDegrees(state.getRobotPosition(myTeam,
				myIdentifier).angleTo(to));
		if (assertFacing(ang, eps)) {
			return true;
		}
		return false;
	}

	/**
	 * Essentially a Point2 abstraction of assertNearForward. Should probably be
	 * refactored or at least renamed.
	 * 
	 * TODO: Consider this.
	 */
	public boolean goTo(Point2 to, double eps) throws Exception {
		if (assertNearForward(to, eps)) {
			return true;
		}
		return false;
	}

	/**
	 * Method for sending the robot to the goalmouth. Must be called
	 * continuously because it is synchronous.
	 * 
	 * Here we suppress all warnings because we compare DEF_ROBOT to 0,
	 * blablabla.
	 */
	@SuppressWarnings("all")
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
				if (turnTo(goal_centre, SAFE_ANGLE_EPSILON)) {
					if (goTo(new Point2(goal_centre.getX() + GOAL_OFFSET,
							goal_centre.getY()), eps)) {
						return true;
					}
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

	/////////////////////////////////////////////////////////////
	// TODO: Check attacker code to conform to defender standards
	/////////////////////////////////////////////////////////////
	
	/**
	 * WIP Attempts to kick the ball by first navigating to a point
	 * (apprachPoint) from which it can then approach the ball and kick it
	 * towards the target goal.
	 * 
	 * TODO:
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
	 **/
	public void kickStationaryBall() throws Exception {
		Point2 robotPosition = state.getRobotPosition(state.getOurColor(),
				state.getDirection());
		Point2 ballPosition = state.getBallPosition();
		Point2 targetPoint = setBallTarget();
		Point2 robotPositionInvertedY = new Point2(robotPosition.getX(),
				(robotPosition.getY() * (-1)));
		Point2 ballPositionInvertedY = new Point2(ballPosition.getX(),
				(ballPosition.getY() * (-1)));
		Point2 targetPointInvertedY = new Point2(targetPoint.getX(),
				(targetPoint.getY() * (-1)));
		// System.out.println("Robot Position :" + robotPosition);
		// System.out.println("Ball Position :" + ballPosition);
		// System.out.println("Ball Target Point :" + targetPoint);

		if (targetPoint.getX() == 0 && targetPoint.getY() == 0) {
			System.out
					.println("Could not assign ball target point within target goal");
			return; // could not set target point for ball and therefore cannot
			// take shot
		}

		int[] vectorBalltoTargetPoint = { // Y coordinates given must be already
				// inverted
				ballPosition.getX() - targetPoint.getX(),
				ballPositionInvertedY.getY() - targetPointInvertedY.getY() };
		int[] vectorBalltoSelectedRobot = {
				ballPosition.getX() - robotPosition.getX(),
				ballPositionInvertedY.getY() - robotPositionInvertedY.getY() };

		double angleBetween = calculateAngle(vectorBalltoTargetPoint,
				vectorBalltoSelectedRobot);

		System.out
				.println("Angle between bot, ball and goal : " + angleBetween);
		// check if the ball will obstruct a direct path to the approach
		// point move to a point where a direct path can be taken to the
		// approach point
		Point2 approachPoint = calculateApproachPoint(ballPositionInvertedY,
				targetPointInvertedY, state.getDirection());
		System.out.println("Ball point: " + ballPosition);
		System.out.println("Assigned approach point: " + approachPoint);
		if ((approachPoint.getX() == 0) && (approachPoint.getY() == 0)) {
			System.out.println("Could not assign Approach Point");
			return;
		}
		// check if the ball will obstruct a direct path to the approach
		// point move to a point where a direct path can be taken to the
		// approach point
		if ((angleBetween > 90)) {
			// Attempt to navigate to the approach point.
			faceAndGoTo(driver, approachPoint);
		} else {
			System.out.println("Need to navigate around ball.");

			// Checks if robot is between the ball and the goal
			// If it is, move up or down
			// *Need to also account for the ball's radius - Robaidh*
			if (Math.abs(robotPosition.y - ballPosition.y) < Constants.ATTACKER_LENGTH) {
				if (robotPosition.y > ballPosition.y) {
					int newy = robotPosition.y + Constants.ATTACKER_LENGTH
							+ SAFE_APPROACH_DIST;
					System.out.println(newy);
					faceAndGoTo(driver, new Point2(robotPosition.x, newy));
				} else {
					int newy = robotPosition.y - Constants.ATTACKER_LENGTH
							- SAFE_APPROACH_DIST;
					System.out.println(newy);
					faceAndGoTo(driver, new Point2(robotPosition.x, newy));
				}
			}
			Thread.sleep(200);
			// Moves horizontally until it aligns with the Approach Point
			Point2 intermediatePoint = new Point2(approachPoint.x,
					robotPosition.y);
			System.out.println("Assigned intermediate point: "
					+ intermediatePoint);
			faceAndGoTo(driver, intermediatePoint);
			Thread.sleep(200);
			faceAndGoTo(driver, approachPoint);

			// Next part is a more complex implementation - must check it out
			/*
			 * Point2 intermediate_point =
			 * calculateIntermediatePoint(ballPosition, targetPoint);
			 * faceAndGoTo(driver, intermediate_point); faceAndGoTo(driver,
			 * approach_point);
			 */
		}

		// Turn towards ball
		while (!turnTo(ballPosition, SAFE_ANGLE_EPSILON)) {
			Thread.sleep(100);
			while (!turnTo(ballPosition, SAFE_ANGLE_EPSILON)) {
				Thread.sleep(100);
			}
			driver.stop();
			Thread.sleep(500);
		}
		Thread.sleep(100);
		driver.stop();

		// Move slightly forward and kick
		driver.forward(80);
		Thread.sleep(1000);
		driver.kick(5000);
		Thread.sleep(100);
		System.out.println("KICK");
		driver.stop();
	}

	/**
	 * Chooses a point within the target goal for the ball to be kicked towards
	 * (auxiliary method) Currently only picks the target goal's centre point
	 * 
	 * @param state
	 * @return
	 */
	public Point2 setBallTarget() {
		if (state.getDirection() == Constants.DIRECTION_RIGHT) // if target goal
																// is the right
																// hand goal
			return state.getRightGoalCentre();
		if (state.getDirection() == Constants.DIRECTION_LEFT)
			return state.getLeftGoalCentre();
		return new Point2(0, 0);
	}

	/**
	 * Calculates the smallest angle between two vectors (auxiliary method)
	 * 
	 * @param vectorA
	 * @param vectorB
	 * @return
	 */
	public static double calculateAngle(int[] vectorA, int[] vectorB) {
		double vectorAMagnitude = Math.sqrt((vectorA[0] * vectorA[0])
				+ (vectorA[1] * vectorA[1]));
		double vectorBMagnitude = Math.sqrt((vectorB[0] * vectorB[0])
				+ (vectorB[1] * vectorB[1]));
		int dotproduct = vectorA[0] * vectorB[0] + vectorA[1] * vectorB[1];
		double angleDegrees = Math.acos(dotproduct
				/ (vectorAMagnitude * vectorBMagnitude))
				* 180 / Math.PI;
		// System.out.println("Vector A Magnitude : " + vectorAMagnitude);
		// System.out.println("Vector B Magnitude : " + vectorBMagnitude);
		// System.out.println("Dot product : " + dotproduct);
		// System.out.println("Angle (in degrees) : " + angleDegrees);
		return angleDegrees;
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
	@SuppressWarnings("unused")
	public static Point2 calculateApproachPoint(Point2 ballPosition,
			Point2 ballTargePoint, int targetGoal) {
		int approachPointX = 0;
		int approachPointY = 0;
		double cutOnYAxis;
		// Bigger ballOffset = further away from the ball
		double ballOffset = 1.0 + ((int) Math.abs(Constants.TABLE_CENTRE_X
				- ballPosition.x)) / 600.0;
		double gradientLineBalltoGoal = calculateGradient(ballPosition.getX(),
				ballPosition.getY(), ballTargePoint.getX(),
				ballTargePoint.getY());
		System.out.println("Ball offset: " + ballOffset);
		if (targetGoal == Constants.DIRECTION_LEFT) {
			approachPointX = (int) (ballPosition.getX() + Math.max(
					Constants.ATTACKER_LENGTH, Constants.ATTACKER_WIDTH)
					* ballOffset);
		} else if (targetGoal == Constants.DIRECTION_RIGHT) {
			approachPointX = (int) (ballPosition.getX() - Math.max(
					Constants.ATTACKER_LENGTH, Constants.ATTACKER_WIDTH)
					* ballOffset);
		}
		cutOnYAxis = ((-1) * (gradientLineBalltoGoal * ballPosition.getX()) + ballPosition
				.getY()); // result should always be <=0
		// approachPointY = (int) (Math.floor(gradientLineBalltoGoal
		// * approachPointX) + cutOnYAxis);
		approachPointY = (int) Math.floor((gradientLineBalltoGoal * 50)
				+ ballPosition.getY());
		return new Point2(approachPointX, (approachPointY * (-1))); // Y
																	// coordinate
																	// must be
																	// inverted
																	// to be
																	// positive
	}

	/**
	 * Faces and travels to the designated point.
	 */
	public void faceAndGoTo(Driver driver, Point2 target)
			throws InterruptedException, Exception {
		System.out.println("Attempting to face: " + target);
		while (!turnTo(target, SAFE_ANGLE_EPSILON)) {
			Thread.sleep(50);
		}
		driver.stop();
		while (!(traveltoPoint(target.getX(), target.getY()))) {
			while (!turnTo(target, SAFE_ANGLE_EPSILON)) {
				Thread.sleep(50);
			}
			driver.stop();
			Thread.sleep(50);
		}
		driver.stop();
	}

	/**
	 * Calculates the gradient between two points (auxiliary method)
	 * 
	 * @param XcoordinatePoint1
	 * @param YcoordinatePoint1
	 * @param XcoordinatePoint2
	 * @param YcoordinatePoint2
	 * @return
	 */
	public static double calculateGradient(int XcoordinatePoint1,
			int YcoordinatePoint1, int XcoordinatePoint2, int YcoordinatePoint2) {
		double diffY = YcoordinatePoint2 - YcoordinatePoint1;
		double diffX = XcoordinatePoint2 - XcoordinatePoint1;
		return (diffY / diffX);
	}

	/**
	 * Orders the attacking robot to travel forwards until it is reasonably
	 * close to the target point. If the attacker robots orientation deviates
	 * from its initial orientation by a set amount then the the robot is
	 * commanded to stop and the travel is reported back as unsuccessful. Then
	 * the a method needs to be called externally to correct the robots
	 * orientation before this method should be called again (auxiliary method)
	 * 
	 * @param driver
	 * @param targetX
	 * @param targetY
	 * @param movingTowardsBall
	 * @return
	 */
	public boolean traveltoPoint(int targetX, int targetY) throws Exception {
		double initialOrientation = state.getRobotFacing(state.getOurColor(),
				state.getDirection());
		Point2 robotPos = state.getRobotPosition(state.getOurColor(),
				state.getDirection());
		double robotFacing = state.getRobotFacing(state.getOurColor(),
				state.getDirection());
		driver.forward(115);
		/*
		 * TODO: Check this out /** Returns a speed for the robot to go forward
		 * at based on how close the robot is to it's target.
		 * 
		 * @param ballPosition
		 * 
		 * @param robotPosition
		 * 
		 * @return
		 *//*
			 * public static int setSpeed(int targetPointX, int targetPointY,
			 * Point2 robotPosition){ double distanceToTarget = 0; int diffX =
			 * Math.abs(targetPointX - robotPosition.getX()); int diffY =
			 * Math.abs(targetPointY - robotPosition.getY()); distanceToTarget =
			 * Math.sqrt(diffX*diffX + diffY*diffY); if (distanceToTarget >
			 * MAX_SPEED_THRESHOLD) return MAX_SPEED; if (distanceToTarget >
			 * MEDIUM_SPEED_THRESHOLD) return MEDIUM_SPEED; return SLOW_SPEED; }
			 * 
			 * public static boolean traveltoPointAlt(Driver driver, int
			 * targetX, int targetY, int movingTowardsBall) throws Exception {
			 * double initialOrientation =
			 * state.getRobotFacing(state.getOurColor(), state.getDirection());
			 * double robotFacing = state.getRobotFacing(state.getOurColor(),
			 * state.getDirection()); Point2 robotPos =
			 * state.getRobotPosition(state.getOurColor(),
			 * state.getDirection()); int threshold = 10; if (movingTowardsBall
			 * == 1){ threshold = SAFE_DIST; } driver.forward(setSpeed(targetX,
			 * targetY, robotPos));
			 */
		while (Math.abs(robotFacing - initialOrientation) < SAFE_ANGLE_EPSILON) {
			if (Math.abs(robotPos.getX() - targetX) < SAFE_APPROACH_DIST
					&& (Math.abs(robotPos.getY() - targetY) < SAFE_APPROACH_DIST)) {
				driver.stop();
				return true;
			}
			robotPos = state.getRobotPosition(state.getOurColor(),
					state.getDirection());
			robotFacing = state.getRobotFacing(state.getOurColor(),
					state.getDirection());
		}
		driver.stop();

		return false;
	}
}
