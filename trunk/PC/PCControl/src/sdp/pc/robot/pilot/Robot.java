package sdp.pc.robot.pilot;

import static sdp.pc.common.Constants.DIRECTION_LEFT;
import static sdp.pc.common.Constants.DIRECTION_RIGHT;
import static sdp.pc.vision.Alg.normalizeToBiDirection;
import static sdp.pc.vision.Alg.normalizeToUnitDegrees;

import java.util.ArrayList;

import sdp.pc.common.Constants;
import sdp.pc.vision.Alg;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.Pitch;
import sdp.pc.vision.Point2;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;

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
	private static final int GOAL_OFFSET = 35;

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
	@SuppressWarnings("unused")
	private static final int BETWEEN_GOALS_EPSILON = 15;

	/**
	 * If an epsilon value doesn't make sense to know in some context, this is a
	 * safe angle value to use
	 */
	private static final double SAFE_ANGLE_EPSILON = 8.0;

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
	 * The most recent calculated state of <b>this</b>
	 */
	private int myState = State.UNKNOWN;
	private static int ms = State.UNKNOWN;

	/**
	 * An incremental "sub-state" value which can be used to handle sub-tasks
	 * within a state. For example, if a robot's state is "pass to attacker",
	 * sub-states could be:
	 * <ol>
	 * <li>Go to the ball</li>
	 * <li>Grab the ball</li>
	 * <li>Turn to the new point</li>
	 * <li>Kick the ball</li>
	 * </ol>
	 */
	private int subState = 0;

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
		if (assertPerpendicular(SAFE_ANGLE_EPSILON)) {
			// Get predicted ball stop point
			Point2 predBallPos = state.getFutureData().getEstimate();

			// If that position exists, go to its Y coordinate, otherwise stop.
			if (!predBallPos.equals(Point2.EMPTY)) {
				if (defendToY(predBallPos.getY(), DEFEND_EPSILON_DISTANCE)) {
					driver.stop();
				}
			} else {
				driver.stop();
			}
		}
	}

	/**
	 * Synchronous method which performs the goal of defending the robot. It
	 * should only be called when the ball is not moving (or ball is not on the
	 * pitch) and the opponent's attacker is on the pitch. It checks the
	 * predicted stop location of the imaginary ball if the attacking robot
	 * would kick now and moves to predicted position's Y coordinate by going
	 * forwards or backwards.
	 * 
	 * This method is for the defending robot. Assumes <b>this</b> is already
	 * perpendicular.
	 */
	public void defendEnemyAttacker() throws Exception {

		// The enemy attacker is the opposite team, and opposite id (since this
		// is a defender)
		defendRobot(1 - this.myTeam, 1 - this.myIdentifier);
	}

	/**
	 * Synchronous method which defends against a given robot. Assumes
	 * <b>this</b> is already perpendicular.
	 * 
	 * @param team
	 * @param robot
	 * @throws Exception
	 */
	public void defendRobot(int team, int robot) throws Exception {

		if (assertPerpendicular(10.0)) {
			// Get my location
			Point2 robotPos = state.getRobotPosition(myTeam, myIdentifier);

			// Get position and facing of the robot we wish to defend against
			Point2 otherPos = state.getRobotPosition(team, robot);
			double otherFacing = state.getRobotFacing(team, robot);

			// Get predicted ball position if that other robot shot just now
			Point2 predictedBallPos = FutureBall.matchingYCoord(otherPos,
					otherFacing, robotPos);

			// If that position exists, defend it, otherwise just defend the
			// ball
			if (!predictedBallPos.equals(Point2.EMPTY)
					& FutureBall.pitchContains(predictedBallPos)) {
				if (defendToY(
						predictedBallPos.offset(50.0,
								predictedBallPos.angleTo(robotPos)).getY(),
						DEFEND_EPSILON_DISTANCE)) {
					driver.stop();
				}
			} else {
				defendBall();
			}
		}
	}

	/**
	 * Synchronous method which must be called continuously which makes a robot
	 * face a specific angle in degrees. The robot will rotate the direction
	 * which gets there faster.
	 * 
	 * @return true if the robot is already facing deg within a window of
	 *         epsilon
	 * @throws Exception
	 */
	public boolean assertFacing(double deg, double epsilon) throws Exception {
		double rotateBy = normalizeToBiDirection(state.getRobotFacing(myTeam,
				myIdentifier) - deg);
		int speed = getRotateSpeed(rotateBy, epsilon);
		if (rotateBy > epsilon) {
			driver.turnLeft(speed);
		} else if (rotateBy < -epsilon) {
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
			return true;
		}
		return false;
	}

	/**
	 * Method for telling the robot to kick the ball to a point
	 * 
	 * @param where
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public void kickBallToPoint(Point2 where) throws Exception {
		// Turn to ball, move to ball, grab the ball, turn to the point, kick
		Point2 ball = state.getBallPosition();
		Point2 robo = state.getRobotPosition(myTeam, myIdentifier);
		double distortion = getDistortion(robo); // distortion return negative
													// values if left of centre
													// and positive if right
		// The offset stuff doesn't really work too well - there are problems
		// with the defender catching the ball
		double xOffset = ((double) (Math.abs(ball.x - Constants.TABLE_CENTRE_X))) / 320;
		double angOffset = 0;
		if (robo.x > Constants.TABLE_CENTRE_X) {
			angOffset = 1 - Math.abs(robo.angleTo(ball)) / 180;
		} else {
			angOffset = Math.abs(robo.angleTo(ball)) / 180;
		}
		xOffset = Math.pow(xOffset, 3);
		angOffset = Math.pow(angOffset, 3);

		if (subState == 0) {
			// if (goTo(ball.offset(20.0, ball.angleTo(robo)), 10.0)) {
			if (goTo(ball, 32 + 5 * xOffset * angOffset)) {
				driver.grab();
				subState = 1;
			}
		}
		// if (subState == 1 && )
		if (subState == 1) {
			if (turnTo(where, 10.0)) {
				driver.kick(900);
				subState = 0;
			}
		}
	}

	/**
	 * Method for telling the robot to kick the ball to a point
	 * 
	 * @param where
	 * @throws Exception
	 */
	public void kickBallToPointWithDistortion(Point2 where) throws Exception {
		// Turn to ball, move to ball, grab the ball, turn to the point, kick
		Point2 ball = state.getBallPosition();
		Point2 robo = state.getRobotPosition(myTeam, myIdentifier);
		double distortion = getDistortion(robo); // distortion return negative
													// values if left of centre
													// and positive if right
		if (subState == 0) {
			// if (goTo(ball.offset(20.0, ball.angleTo(robo)), 10.0)) {
			ball.setX((int) Math.round(ball.getX() - distortion));
			if (goTo(ball, 30.0)) {
				driver.grab();
				subState = 1;
			}
		}
		if (subState == 1) {
			if (turnTo(where, 10.0)) {
				driver.kick(900);
				subState = 0;
			}
		}
	}

	/**
	 * Kicks stationary ball. Assumes robot is already at the approach point.
	 * 
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
	 * Not using it atm - don't touch unless you're Iain
	 */
	@SuppressWarnings("unused")
	public boolean passBall() throws Exception {
		// Turn to ball, move to ball, grab the ball, turn to the point, kick
		Point2 ball = state.getBallPosition();
		Point2 robo = state.getRobotPosition(myTeam, myIdentifier);

		Point2 where = getPassPoint();
		if (subState == 0) {
			// if (goTo(ball.offset(20.0, ball.angleTo(robo)), 20.0)) {
			if (goTo(ball, 30.0)) {
				driver.grab();
				subState = 1;
			}
		}
		if (subState == 1) {
			if (turnTo(where, 10.0)) {
				driver.kick(900);
				subState = 0;
			}
		}
		return true;

	}

	/**
	 * Not using it atm - don't touch unless you're Iain
	 */
	public Point2 getPassPoint() {
		// get shooting direction.
		// get enemy attacker position
		Pitch pitch = state.getPitch();
		Point2 pass = Point2.EMPTY;
		int passY, passX;
		state.getDirection();
		ArrayList<Point2> points = pitch.getArrayListOfPoints();
		Point2 robo = state.getRobotPosition(1 - myTeam, 1 - myIdentifier);
		Boolean bottom = false;
		if (robo.getY() > 225) {
			bottom = true;
		}

		if (state.getDirection() == 0) {
			// shooting right
			passX = ((points.get(4).getX() - points.get(3).getX()) / 2)
					+ points.get(3).getX();
			if (bottom) {
				passY = points.get(4).getY() + 40;
			} else {
				passY = points.get(9).getY() - 40;
			}
		} else {
			passX = ((points.get(3).getX() - points.get(3).getX()) / 2)
					+ points.get(2).getX();
			if (bottom) {
				passY = points.get(2).getY() + 40;
			} else {
				passY = points.get(11).getY() - 40;
			}
		}
		pass.setX(passX);
		pass.setY(passY);
		return pass;

	}

	/**
	 * Not using it atm - don't touch unless you're Iain
	 */
	public Boolean recievePass() {
		Point2 where = getPassPoint();
		try {
			goTo(where, 10.0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}

	/**
	 * Get the most recent calculated state of <b>this</b>
	 * 
	 * @return
	 */
	public int getState() {
		return this.myState;
	}

	/**
	 * Set <b>this</b> to have a new state
	 * 
	 * @param newState
	 */
	public void setState(int newState) {
		this.myState = newState;
		ms = newState;
	}

	/**
	 * Getter method for the sub-state of the robot.
	 * 
	 * @return
	 */
	public int getSubState() {
		return this.subState;
	}

	/**
	 * Setter method for the sub-state of the robot.
	 */
	public void setSubState(int s) {
		this.subState = s;
	}

	/**
	 * Makes the robot, which should already be perpendicular, move forward or
	 * backward to cut off the estimated ball postion's Y coordinate. The method
	 * is synchronous, and therefore must be called until it returns true if you
	 * expect it to be finished.
	 */
	private boolean defendToY(int y, double eps) throws Exception {

		Point2 botPosition = state.getRobotPosition(myTeam, myIdentifier);
		double botFacing = state.getRobotFacing(myTeam, myIdentifier);
		if (botPosition.y - y > DEFEND_EPSILON_DISTANCE) {
			if (Alg.withinBounds((float) botFacing, 270,
					(float) SAFE_ANGLE_EPSILON)) {
				moveForwardTo(new Point2(botPosition.x, y),
						DEFEND_EPSILON_DISTANCE);
			} else if (Alg.withinBounds((float) botFacing, 90,
					(float) SAFE_ANGLE_EPSILON)) {
				moveBackwardTo(new Point2(botPosition.x, y),
						DEFEND_EPSILON_DISTANCE);
			}
			return false;
		}
		if (y - botPosition.y > DEFEND_EPSILON_DISTANCE) {
			if (Alg.withinBounds((float) botFacing, 270,
					(float) SAFE_ANGLE_EPSILON)) {
				moveBackwardTo(new Point2(botPosition.x, y),
						DEFEND_EPSILON_DISTANCE);
			} else if (Alg.withinBounds((float) botFacing, 90,
					(float) SAFE_ANGLE_EPSILON)) {
				moveForwardTo(new Point2(botPosition.x, y),
						DEFEND_EPSILON_DISTANCE);
			}
			return false;
		}
		driver.stop();
		return true;
	}

	/**
	 * Returns the desired rotate speed based on how far the robot has to rotate
	 * before it finishes. Useful because there is a delay associated with
	 * frames of world state, and we can't rotate at full-speed all the time.
	 * 
	 * @return TODO: speed in motor-degrees (?) per second
	 */
	private static int getRotateSpeed(double rotateBy, double epsilon) {
		rotateBy = Math.abs(rotateBy);
		if (rotateBy > 75.0) {
			return 120;
		} else if (rotateBy > 30.0) {
			return 60;
		} else if (rotateBy > epsilon) {
			return 20;
		} else {
			return 0;
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
		int speed = getMoveSpeed(robLoc.distance(to));
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
		int speed = getMoveSpeed(robLoc.distance(to));
		driver.backward(speed);
		return false;
	}

	/**
	 * Similar to getRotateSpeed, gets a reasonable move speed for the robot
	 * depending how far it is from the target.
	 * 
	 * TODO: Units? motor velocity in radians per second or..?
	 */
	private static int getMoveSpeed(double distance) {
		if (ms == State.DEFEND_BALL || ms == State.DEFEND_ENEMY_ATTACKER) {
			return (int) ((distance + 30) * 2.5);
		}
		if (distance > 180.0) {
			return 300;
		} else if (distance > 120.0) {
			return 150;
		} else if (distance > 50.0) {
			return 60;
		} else {
			return 30;
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
	// TODO: No longer used, but maybe necessary
	@SuppressWarnings("unused")
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
	// TODO: Method no longer used, but seems useful..
	@SuppressWarnings("unused")
	private static double calculateAngle(Point2 vecA, Point2 vecB) {
		double vecAMagnitude = vecA.modulus();
		double vecBMagnitude = vecB.modulus();

		int dotProduct = vecA.dot(vecB);

		double ang = Math.acos(dotProduct / (vecAMagnitude * vecBMagnitude));

		return Math.toDegrees(ang);
	}

	/**
	 * Static class for referencing robot states once we start making the AI
	 * decision model. Should probably be an Enum but I can't be bothered.
	 * 
	 * @author s1143704
	 */
	public static class State {
		public static int UNKNOWN = 0;
		public static int DEFEND_BALL = 1;
		public static int DEFEND_ENEMY_ATTACKER = 2;
		public static int WAIT_RECEIVE_PASS = 3;
		public static int DEFEND_GOAL_LINE = 4;
		public static int PASS_TO_ATTACKER = 5;
		public static int DEFEND_ENEMY_DEFENDER = 6;
		public static int GET_BALL = 7;
		public static int ATTEMPT_GOAL = 8;

		public static void print(int q) {
			switch (q) {
			case 0:
				System.out.println("UNKNOWN");
				break;
			case 1:
				System.out.println("DEFEND_BALL");
				break;
			case 2:
				System.out.println("DEFEND_ENEMY_ATTACKER");
				break;
			case 3:
				System.out.println("WAIT_RECEIVE_PASS");
				break;
			case 4:
				System.out.println("DEFEND_GOAL_LINE");
				break;
			case 5:
				System.out.println("PASS_TO_ATTACKER");
				break;
			case 6:
				System.out.println("DEFEND_ENEMY_DEFENDER");
				break;
			case 7:
				System.out.println("GET_BALL");
				break;
			case 8:
				System.out.println("ATTEMPT_GOAL");
				break;
			}

		}
	}

	@SuppressWarnings("unused")
	public static double getDistortion(Point2 point) {
		double distanceToGoal = 250;
		double maxDistortion = 25;
		double midDistortion = 10;
		double distortion;
		if (point.getX() - 250 > 0) {
			double distanceRight = point.getX() - 313;
			distortion = (maxDistortion / 100)
					* (distanceRight / distanceToGoal);
			return distortion;
		} else {
			double distanceLeft = 313 - point.getX();
			distortion = (-1) * (maxDistortion / 100)
					* (distanceLeft / distanceToGoal);
			return distortion;
		}
	}

	/**
	 * Returns this robot's team.
	 * 
	 * @return
	 */
	public int getTeam() {
		return this.myTeam;
	}

	/**
	 * Returns the ID of the opposite robot. If <b>this</b>'s id is 0, returns
	 * 1, and 0 otherwise.
	 * 
	 * @return
	 */
	public int getOtherId() {
		return 1 - this.myIdentifier;
	}

	/**
	 * Returns the team of the opposite team. If <b>this</b>'s team is 0,
	 * returns 1, and 0 otherwise.
	 * 
	 * @return
	 */
	public int getOtherTeam() {
		return 1 - this.myTeam;
	}
}
