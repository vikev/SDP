package sdp.pc.robot.pilot;

import static sdp.pc.vision.Alg.normalizeToBiDirection;
import static sdp.pc.vision.Alg.normalizeToUnitDegrees;
import static sdp.pc.vision.FutureBall.getDeflectionAngle;

import java.util.ArrayList;
import java.util.LinkedList;

import sdp.pc.vision.Alg;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.Pitch;
import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;

/**
 * An abstract class for conducting non-trivial orders on a Robot. A delegator
 * for a Driver.
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
	 * If an epsilon value doesn't make sense to know in some context, this is a
	 * safe angle value to use
	 */
	private static final double SAFE_ANGLE_EPSILON = 8.0;

	/**
	 * An epsilon value for the robot to be perpendicular in the defendBall
	 * method
	 */
	private static final double DEFEND_ANGLE_EPSILON = 16.0;

	/**
	 * Determines how much you want to squeeze the quadrants (for making sure
	 * the robots avoid their boundaries)
	 */
	private static final double HULL_OFFSET = 60.0;

	/**
	 * Safe distance between the enemy attacker, and a straight line between our
	 * robots, in order to decide if a direct pass is possible.
	 */
	private static final double SAFE_PASS_DISTANCE = 40.0;

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
	 * This is just an identifier for a robot's previous quadrant position. If
	 * this value is different from getMyQuadrant (and not 0), we print a
	 * warning that the robot has changed quadrants.
	 */
	private int lastQuadrant = 0;

	/**
	 * An integer which represents <b>this</> with respect to myTeam.
	 */
	private int myIdentifier;

	/**
	 * The most recent calculated state of <b>this</b>
	 */
	private int myState = State.UNKNOWN;

	private int prevState = State.UNKNOWN;

	/**
	 * An incremental substate for the defendBall method. TODO: What does the
	 * defendBall method use substates for?
	 */
	private int defendSubState = 0;

	/**
	 * An incremental "sub-state" value denoting which sub-task has been
	 * performed or is being performed within the kickBallToPoint method.
	 * Current states are:
	 * <ol>
	 * <li>Going to the ball</li>
	 * <li>Think we have grabbed the ball</li>
	 * </ol>
	 * TODO: There are many new substates; documentation should be updated
	 */
	private int kickSubState = 0;

	/**
	 * A substate used in the goTo method, which helps prevent the robot from
	 * overturning. TODO: What are those states?
	 */
	private int turnSubState = 0;

	/**
	 * A substate used for shooting strategy. TODO: What are the states?
	 */
	private int shootStratSubState = 0;

	/**
	 * The most recently calculated bounce Point
	 */
	public Point2 bouncePoint = new Point2(0, 0);

	/**
	 * The point the defender was at when bouncePoint was calculated
	 */
	public Point2 defenderPosWhenBouncePointcalc = new Point2(0, 0);

	/**
	 * Part of the shooting strategy; a flag which makes the attacker aim to the
	 * top of the goal first, potentially making the opposing defender get out
	 * of position.
	 */
	public boolean turnedTowardsTopOfGoal = false;

	/**
	 * Part of the shooting strategy; a flag which tells if the attacker is in
	 * position to shoot.
	 */
	public boolean onShootPoint = false;

	/**
	 * A point above the opposing goal.
	 */
	Point2 topCornerOfGoal = new Point2(0, 0);

	/**
	 * A point below the opposing goal.
	 */
	Point2 bottomCornerOfGoal = new Point2(0, 0);

	/**
	 * Some specific point near the goal centre where we want our attacker to
	 * shoot from. Part of shooting strategy.
	 */
	Point2 shootPoint = new Point2(0, 0);

	/**
	 * A flag which tells if the shooting strategy is initialized.
	 */
	boolean shootStratInitalised = false;

	/**
	 * TODO: No idea. Something to do with shooting strategy.
	 */
	boolean shootBot = false;

	/**
	 * TODO: Something to do with wall kick strategy.. no idea.
	 */
	public int adjustKickPoint = 10;
	

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

	/**
	 * Orders the robot's driver to simply stop.
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception {
		driver.stop();
	}

	/**
	 * Closes the connection to the robot.
	 */
	public void closeConnection() {
		driver.closeConnection();
	}

	/**
	 * Synchronous method which performs the goal of defending the ball. It
	 * checks the predicted stop location of the ball and moves to its Y
	 * coordinate by going forwards or backwards.
	 */
	public void defendBall() throws Exception {
		if (assertPerpendicular(DEFEND_ANGLE_EPSILON)) {

			// Get predicted ball stop point
			// TODO: Needs to be an intersection of the future data with the
			// robot position.
			int myX = state.getRobotPosition(myTeam, myIdentifier).getX();
			Point2 predBallPos = state.getFutureData().getEstimateIntersectX(myX);

			// If that position exists, go to its Y coordinate, otherwise stop.
			if (!predBallPos.equals(Point2.EMPTY)) {
				int destination = predBallPos.getY();
				if (!Alg.inMinorHull(getQuadrantVertices(getMyQuadrant()),
						10.0, predBallPos)) {
					destination = predBallPos.offset(
							20.0,
							predBallPos.angleTo(state.getPitch()
									.getQuadrantCenter(getMyQuadrant())))
							.getY();
				}
				if (defendToY(destination, DEFEND_EPSILON_DISTANCE)) {
					driver.stop();
					defendSubState++;
					if (defendSubState > 20) {
						goTo(state.getPitch()
								.getQuadrantCenter(getMyQuadrant()), 10.0);
					} else if (defendSubState > 50) {
						defendSubState = 0;
					}
				}
			} else {
				driver.stop();
			}
		}
	}

	/**
	 * Defends a goal line weighting between the goal line's centre and the
	 * estimated ball position.
	 * 
	 * @param weight
	 *            - how much to weight the goal centre a weight of 1.0
	 *            essentially goes to the goal centre)
	 * @throws Exception
	 */
	public void defendWeightedGoalLine(double weight) throws Exception {
		assert (weight <= 1.0);
		if (assertNearGoalLine(10.0)) {
			if (assertPerpendicular(10.0)) {
				double y = 0;
				y += state.getPitch().getRightGoalCentre().getY() * weight;
				y += state.getFutureData().getEstimate().getY()
						* (1.0 - weight);
				defendToY((int) y, 10.0);
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

		if (assertPerpendicular(2 * SAFE_ANGLE_EPSILON)) {
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
				if (defendToY(predictedBallPos.getY(), DEFEND_EPSILON_DISTANCE)) {
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
	 * Makes the robot face the point + 180 (non-blocking)
	 * 
	 * @param to
	 * @param eps
	 * @return
	 * @throws Exception
	 */
	public boolean turnToReverse(Point2 to, double eps) throws Exception {
		double ang = normalizeToUnitDegrees(state.getRobotPosition(myTeam,
				myIdentifier).angleTo(to) + 180.0);
		if (assertFacing(ang, eps)) {
			return true;
		}
		return false;
	}

	/**
	 * A method which signals the robot to go to a point the fastest way
	 * possible. Checks the facing angle of the robot and decides to go forwards
	 * or backwards to get there.
	 * 
	 * @param to
	 * @param eps
	 * @return
	 * @throws Exception
	 */
	public boolean goToFast(Point2 to, double eps) throws Exception {

		// Get the necessary angles
		double angleToPoint = normalizeToUnitDegrees(state.getRobotPosition(
				myTeam, myIdentifier).angleTo(to));
		double facing = normalizeToUnitDegrees(state.getRobotFacing(myTeam,
				myIdentifier));

		// Compare them
		double diff = normalizeToUnitDegrees(facing - angleToPoint);
		if (Math.abs(diff) > 90.0) {
			return goToReverse(to, eps);
		}
		return goTo(to, eps);
	}

	/**
	 * Makes the robot turn to the point then move forward to the point (returns
	 * true if complete)1
	 */
	public boolean goTo(Point2 to, double eps) throws Exception {
		int was = turnSubState;

		if (turnTo(to, SAFE_ANGLE_EPSILON)) {
			turnSubState = 1;
			if (was != turnSubState) {
				driver.stop();
			}
			if (moveForwardTo(to, eps)) {
				return true;
			}
		} else {
			turnSubState = 0;
		}
		return false;
	}

	/**
	 * Makes the robot turn backwards with respect to a point, and then move
	 * backwards to it (non-blocking)
	 * 
	 * @param to
	 * @param eps
	 * @return
	 * @throws Exception
	 */
	public boolean goToReverse(Point2 to, double eps) throws Exception {
		if (turnToReverse(to, SAFE_ANGLE_EPSILON)) {
			if (moveBackwardTo(to, eps)) {
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
	 * 
	 * @param eps
	 *            - Epsilon window for how close to the goal line centre to get
	 *            <i>in the case that the robot isn't already near the goal
	 *            line</i>
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

	public void grabBall() throws Exception {
		Point2 ball = state.getBallPosition();
		Point2 currentPos = state.getRobotPosition(getTeam(), getId());
		if (goTo(ball.offset(25.0, ball.angleTo(currentPos)), 10.0)) {
			driver.stop();
			driver.grab();
		}
	}

	/**
	 * Method for telling the robot to kick the ball to a point
	 * 
	 * @param where
	 * @throws Exception
	 */
	public void kickBallToPoint(Point2 where, boolean justGrab) throws Exception {

		// Turn to ball, move to ball, grab the ball, turn to the point, kick
		Point2 ball = state.getBallPosition();
		Point2 pos = state.getRobotPosition(getTeam(), getId());
		int pitchId = state.getPitchId();

		// If not grabbed ball yet
		if (kickSubState <= 0) {

			// If ball close to wall
			if (!Alg.inMinorHullWeighted(
					getQuadrantVerticesWide(getMyQuadrant()), 20, ball, 1, 0.05)) {
				// First go to x-coordinate of ball, then to ball itself
				if (kickSubState == 0)
					kickSubState = -2;
				if (kickSubState == -2) {
					if (goTo(new Point2(pos.x, ball.y), 20.0)) {
						kickSubState = -1;
					}
				}
				if (kickSubState == -1) {

					// Offset ball slightly away from boundary
					/*ball = new Point2(
							ball.offset(4, ball.angleTo(state.getPitch()
									.getQuadrantCentres()[getMyQuadrant() - 1])).x,
							ball.y);*/

					// If close to ball, grab
					// Once getPitchId is fixed not to return only 0, we can put the following
					// in the constants class
					System.out.println("Check if close to ball");
					if (goTo(
							//ball.offset(18.0 + 0 * pitchId, ball.angleTo(pos)), // side pitch
							ball.offset(25.0 + 0 * pitchId, ball.angleTo(pos)), // main pitch
							10.0)) {

						driver.stop();
						System.out.println("Grab!");
						driver.grab();

						// driver.backward(10);
						kickSubState = 1;
					}
				}
				// If ball is not close to wall, do it simply
			} else {
				if (kickSubState < 0)
					kickSubState = 0;
				if (goTo(
						//ball.offset(20.0 + 0 * pitchId, ball.angleTo(pos)), //side pitch
						ball.offset(20.0 + 0 * pitchId, ball.angleTo(pos)), //main pitch
						10.0)) {

					driver.stop();
					driver.grab();
					kickSubState = 1;
				}
			}
		}
		if (kickSubState >= 1 && kickSubState < 5)
			kickSubState++;
		if (kickSubState >= 5) {
			if(checkHoldingBall(pos, ball)){
				if(!justGrab){
					// If this is attacker, do Shoot Strategy
					if (myIdentifier == state.getDirection()) {
						kickGrabbedBallTo(where);
					} else {
						// If defender, execute pass strategy
						passStrategy(where);
					}
				}
			}
		}
	}

	/**
	 * Checks if the specified robot has the ball, resets kickSubState if it
	 * doesn't.
	 * 
	 * @param robotPos
	 * @param ballPos
	 * @throws Exception
	 */
	public boolean checkHoldingBall(Point2 robotPos, Point2 ballPos)
			throws Exception {
		if (robotPos.distance(ballPos) > 50 && !ballPos.equals(Point2.EMPTY)) {
			System.out.println("Distance: " + robotPos.distance(ballPos));
			System.out.println("Robotpos: " + robotPos);
			System.out.println("ballpos: " + ballPos);
			driver.stop();
			driver.open();
			kickSubState = 0;
			return true;
			// TODO: If robot has the ball, always say that ball is in front of
			// robot
		}
		return false;
	}

	public boolean closeTo(Point2 source, Point2 dest, Point2 obstacle) {
		Point2 p = source.subtract(dest);
		Point2 p1 = source.subtract(obstacle);

		return ((p.x * p1.y - p.y * p1.x) / (Math.sqrt(p.x * p.x + p.y * p.y))
				+ 0.0000001 <= 10);
	}

	/**
	 * Kicks an already grabbed ball. Assumes robot is already at the shoot
	 * point.
	 * 
	 * @param target
	 *            - point to kick the ball to
	 * @param shootStratInitalised
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public boolean kickGrabbedBallTo(Point2 target)
			throws InterruptedException, Exception {
		if (turnTo(target, 8)) {
			driver.forward(100);
			driver.kick(900);
			driver.stop();

			return true;

		}
		return false;
	}

	private void initScorePoints() {
		shootPoint = state.getPitch().getQuadrantCentres()[getMyQuadrant() - 1]
				.copy();

		// Calculate shoot point and goal corners
		if (state.getDirection() == 1) {
			topCornerOfGoal = state.getRightGoalCentre();
			topCornerOfGoal.setY(topCornerOfGoal.getY() + 25);
			bottomCornerOfGoal = state.getRightGoalCentre();
			bottomCornerOfGoal.setY(bottomCornerOfGoal.getY() - 25);
			shootPoint.setX(shootPoint.getX() + 20);
		} else {
			topCornerOfGoal = state.getLeftGoalCentre();
			topCornerOfGoal.setY(topCornerOfGoal.getY() + 25);
			bottomCornerOfGoal = state.getLeftGoalCentre();
			bottomCornerOfGoal.setY(bottomCornerOfGoal.getY() - 25);
			shootPoint.setX(shootPoint.getX() - 20);
		}
		shootStratInitalised = true;
	}

	/**
	 * Attempts to score by first navigating to a point next to the opposing
	 * defenders zone in the centre of the our attackers quadrant. It then turns
	 * the robot to face the top corner of the opposing goal in an attempt to
	 * move the opposing defender into a position away from the centre of the
	 * goal. If the defender moves to block. The robot then kicks the ball
	 * towards the opposite corner.
	 * 
	 * @throws Exception
	 * @throws InterruptedException
	 */
	public void shootStrategy1() throws InterruptedException, Exception {
		if (!shootStratInitalised)
			initScorePoints();

		// Check we still have the ball
		checkHoldingBall(state.getRobotPosition(getTeam(), myIdentifier),
				state.getBallPosition());

		// Move to a point close to the opposing defender's zone that also
		// has the y coordinate of the centre of our attackers quadrant.
		if (onShootPoint == true) {
			// Attempt to make opposing defender go into the top corner of the
			// goal
			if (turnedTowardsTopOfGoal) {
				if (shootStratSubState > 3) {
					if (shootBot
							|| isEnemyBotBlocking(
									state.getRobotPosition(getOtherTeam(),
											myIdentifier),
									state.getRobotPosition(myTeam, myIdentifier),
									topCornerOfGoal)) {
						shootBot = true;
						if (kickGrabbedBallTo(bottomCornerOfGoal)) {
							shootStratSubState++;
							driver.stop();
							if (shootStratSubState > 5) {
								// Reset variables
								shootBot = false;
								shootStratInitalised = false;
								shootStratSubState = 0;
								kickSubState = 0;
								turnedTowardsTopOfGoal = false;
								onShootPoint = false;
							}
						}
					} else {
						if (kickGrabbedBallTo(topCornerOfGoal)) {
							// avoid moving when kicking the ball
							shootStratSubState++;
							if (shootStratSubState > 5) {
								// Reset variables
								shootBot = false;
								shootStratInitalised = false;
								shootStratSubState = 0;
								kickSubState = 0;
								turnedTowardsTopOfGoal = false;
								onShootPoint = false;
							}
						}
					}
				} else {
					shootStratSubState++;
				}
			} else if (turnTo(topCornerOfGoal, 5)) {
				turnedTowardsTopOfGoal = true;
			}
		} else if (goTo(shootPoint, 10)) {
			onShootPoint = true;
		}
	}

	public void passStrategy(Point2 where) throws Exception {
		if (kickSubState >= 5 && kickSubState < 9) {
			if (turnTo(where, 9.0)) {
				driver.stop();
				kickSubState++;
			}
		} else if (kickSubState >= 9) {
			driver.stop();
			driver.kick(900);
			kickSubState++;
			if (kickSubState >= 15) {
				kickSubState = 0;
			}
		}
	}

	/**
	 * Returns the centre of the goal of their defender
	 * 
	 * @return
	 */
	private Point2 getTheirGoalCentre() {
		if (state.getDirection() == 0) {
			return state.getLeftGoalCentre();
		}
		return state.getRightGoalCentre();
	}

	/**
	 * Passes the ball from our defender to our attacker. Either with a direct
	 * pass between them or with a bounce pass.
	 * 
	 * @throws Exception
	 */
	public void defenderPass() throws Exception {
		Point2 ourDefender = state.getRobotPosition(myTeam, myIdentifier);
		// our Attackers Position
		Point2 target = state.getRobotPosition(myTeam, 1 - myIdentifier).copy();
		Point2 enemyAttacker = state.getRobotPosition(1 - myTeam, myIdentifier);

		// If our attacker is not on the pitch (or just cannot recognise him)
		// - change target to goal target
		if (target.equals(Point2.EMPTY)) {
			target = getTheirGoalCentre();
		}
		// Check if there is opponents attacker between our defender and target
		// and if yes - do bounce shot.
		if (isEnemyBotBlocking(enemyAttacker, ourDefender, target)) {
			setBouncePoint(enemyAttacker, ourDefender, target);
			//bouncePoint = wallKick(ourDefender, target, enemyAttacker);
			System.out.println("Bounce: " + bouncePoint);
			kickBallToPoint(bouncePoint,false);
		} else {
			// Otherwise - just kick straight to the target point
			kickBallToPoint(target,false);

			System.out.println("Straight: " + target);
		}
	}

	/**
	 * source - state.getRobotPosition(myTeam, attackerId); dest -
	 * state.getRobotPosition(1 - myTeam,1 - defenderId);
	 * 
	 * @param Point2
	 *            source
	 * @param Point2
	 *            dest
	 * @return
	 */
	public Point2 wallKick(Point2 source, Point2 dest, Point2 obstacle) {
		ArrayList<Point2> pts = state.getPitch().getArrayListOfPoints();
		Point2 leftShootPos = new Point2((int) (source.x + dest.x) / 2,
				pts.get(2).y);
		Point2 rightShootPos = new Point2((int) (source.x + dest.x) / 2,
				pts.get(9).y);

		if (state.getDirection() == 1) {
			leftShootPos.x -= adjustKickPoint;
			rightShootPos.x -= adjustKickPoint;
		} else {
			leftShootPos.x += adjustKickPoint;
			rightShootPos.x += adjustKickPoint;
		}

		double angLeftOfDefender = (obstacle.subtract(source))
				.angleBetween(leftShootPos.subtract(source));
		double angRightOfDefender = (obstacle.subtract(source))
				.angleBetween(rightShootPos.subtract(source));

		if (angLeftOfDefender + 0.000001 < angRightOfDefender)
			return rightShootPos;

		return leftShootPos;

	}

	/**
	 * Attempts to discern whether the enemy team's defender would block the
	 * ball if it was kicked to the target point. Currently only uses
	 * safe(large) estimates for the opposing robots dimensions. Calculates if a
	 * line drawn between our robot and the target point intersects with any of
	 * the edges of a box drawn around the enemy defender.
	 * 
	 * @param enemyDefender
	 * @param ourRobot
	 * @param target
	 * @return
	 */
	public static boolean isEnemyBotBlocking(Point2 enemyRobot,
			Point2 ourRobot, Point2 target) {
		double a = ourRobot.distance(enemyRobot);
		double b = target.distance(enemyRobot);
		double c = ourRobot.distance(target);
		// Trigonometry FTW!
		double x = a
				* Math.sin(Math.acos((a * a + c * c - b * b) / (2 * a * c)));
		if (x < SAFE_PASS_DISTANCE) {
			return true;
		}
		return false;
	}

	/**
	 * Searches for a bounce point on one of the long sides of the pitch that
	 * would allow the ball to be deflected towards our attacker robot if our
	 * defender were to turn and kick the ball to that point. TODO:Find a way to
	 * get a y coordinate on either boundary that corresponds to testx's value
	 * TODO:Decide whether or not to add early termination to the x coordinate
	 * binary search by using an angle threshold
	 * 
	 * @param enemyAttacker
	 * @param ourDefender
	 * @param ourAttacker
	 */
	public void setBouncePoint(Point2 enemyAttacker, Point2 ourDefender,
			Point2 ourAttacker) {
		int testy = 0, testx = ourDefender.getX(), rangeEnd = ourAttacker
				.getX(), rangeStart = ourDefender.getX(), midOfRange, length = Math
				.abs(rangeStart - rangeEnd);
		double bestAngleDiff = 360, requiredAngle, deflectAngle, angleDiff;

		// Sets bestPoint to a default value that will make our
		// defender perform a direct pass to our attacker if no bounce point is
		// found
		Point2 bestPoint = new Point2(ourAttacker.getX(), ourAttacker.getY());
		// System.out.println("initial" + bestPoint);

		Point2 testPoint = new Point2(testx, testy);
		Pitch pitch = state.getPitch();
		int scoreDirection = state.getDirection(); // 0 for left
		boolean directionisleft = (scoreDirection == 0);
		boolean bouncetop = false;

		// Pick which boundary of the pitch to bounce the ball off
		// and set bounce point's y coordinates
		if (enemyAttacker.getY() > pitch.getTableCentre().getY()) {
			bouncetop = true;
			if (state.getPitchId() == 0) {
				testy = 70;
			} else {
				testy = 96;
			}
		} else {
			if (state.getPitchId() == 0) {
				testy = 355;
			} else {
				testy = 392;
			}
		}
		testPoint.setY(testy);

		// Search for a point along one of the pitch boundaries that our
		// defender will
		// be able to use to successfully bounce pass to our attacker
		// Note: the testY values are not exactly on either boundary
		// so angleDiff's value will be off from the true value
		// Assumes a linear relationship between the predicted deflection angle
		// and
		// the x coordinate of the test point
		while (!(length == 1)) {
			if (directionisleft) {
				midOfRange = rangeStart - ((int) Math.floor(length / 2));
				testx = midOfRange;
				testPoint.setX(testx);
			} else {
				midOfRange = rangeStart + ((int) Math.floor(length / 2));
				testx = midOfRange;
				testPoint.setX(testx);
			}

			// Predict the angle the ball would bounce off the boundary
			// at if kicked towards the point on the boundary
			// nearest the current test point
			deflectAngle = getDeflectionAngle(ourDefender, testPoint);

			// Calculate the angle from the test point to the attacker
			// (need to convert the angle by 180 degrees due to the
			// angle measured by getDeflectionAngle having a different zero
			// point)
			if (bouncetop) {
				requiredAngle = testPoint.angleTo(ourAttacker) - 180;
			} else {
				requiredAngle = testPoint.angleTo(ourAttacker) + 180;
			}
			angleDiff = Math.abs(requiredAngle - deflectAngle);

			/*
			 * System.out.println("Attempt Number:" + attempt);
			 * System.out.println("test Point: " + testPoint);
			 * System.out.println("Predicted Deflection Angle: " +
			 * deflectAngle); System.out.println("Required Deflection Angle:" +
			 * requiredAngle); System.out.println("Difference: " + angleDiff +
			 * "\n"); attempt++;
			 */
			if (bouncetop) {
				if (deflectAngle > requiredAngle) {
					if (!directionisleft) {
						rangeStart = midOfRange;
					} else {
						rangeEnd = midOfRange;
					}
				} else {
					if (!directionisleft) {
						rangeEnd = midOfRange;
					} else {
						rangeStart = midOfRange;
					}
				}
			} else {
				if (deflectAngle < requiredAngle) {
					if (!directionisleft) {
						rangeStart = midOfRange;
					} else {
						rangeEnd = midOfRange;
					}
				} else {
					if (!directionisleft) {
						rangeEnd = midOfRange;
					} else {
						rangeStart = midOfRange;
					}
				}
			}

			if (angleDiff < bestAngleDiff) {
				bestPoint.setX(testx);
				bestPoint.setY(testy);
				bestAngleDiff = angleDiff;
			}
			length = Math.abs(rangeStart - rangeEnd);
		}
		bouncePoint = new Point2(bestPoint.getX(), bestPoint.getY());
		// System.out.println("best" + bestPoint);
		defenderPosWhenBouncePointcalc = ourDefender;

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
			e.printStackTrace();
		}
		return true;

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
	private int getRotateSpeed(double rotateBy, double epsilon) {
		// TODO: Should be refactored (constants)
		double maxSpeed = 120.0;
		double minSpeed = 15.0;
		double maxRotate = 180.0;
		double minRotate = epsilon;
		rotateBy = Math.abs(rotateBy);
		int speed = (int) ((maxSpeed - minSpeed) / (maxRotate - minRotate)
				* (rotateBy - minRotate) + minSpeed);

		// If the value is under the epsilon distance don't turn at all
		if (rotateBy < epsilon) {
			return 0;
		}
		
		if (onShootPoint) {
			return speed + 30;
		}

		// Don't change this, just change the constants.
		return speed;

		
		 //function that has a sharper increase in returned values compared to
		 //the above function, at small angels.
/*		 int divFactor = 5;
		 int multFactor = 20;
		 minSpeed = 4.0;
		 if(rotateBy >= divFactor ){
			 if(rotateBy < 60){
				return (int)
			 (Math.log(rotateBy/divFactor)*multFactor + minSpeed);
			 }else {
				 return	 (int) maxSpeed;
			 }
		 }else{
			 return (int) minSpeed;
		 }*/
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
	private int getMoveSpeed(double dist) {

		// TODO: Need to test with final gear ratios and robots! Also refactor
		// out these constants
		double maxSpeed = 300.0;
		double minSpeed = 30.0;
		double maxDist = 150.0;
		dist = Math.abs(dist);
		// Don't change this! Change the constants
		int speed = (int) (((maxSpeed - minSpeed) / (maxDist) * dist) + minSpeed);

		if (myState != State.GET_BALL && myState != State.PASS_TO_ATTACKER) {
			return speed + 120;
		} else if (kickSubState < 0) {
			return (int) (speed / 1.5);
		} else {
			return speed;
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
		public static int DO_NOTHING = 9;
		public static int RESET = 10;

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
			case 9:
				System.out.println("DO_NOTHING");
				break;
			case 10:
				System.out.println("RESET");
				break;
			}

		}
	}

	public static double getDistortion(Point2 point) {
		double maxDist = 250.0;
		double maxScale = 1.5;

		return (maxScale - 1.0) / maxDist
				* point.distance(Vision.getCameraCentre()) + 1.0;
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
	 * Returns the robot's identifier.
	 * 
	 */
	public int getId() {
		return this.myIdentifier;
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

	public Driver getDriver() {
		return this.driver;
	}

	/**
	 * Returns the quadrant <b>this</b> is in.
	 * 
	 * @return
	 */
	public int getMyQuadrant() {
		return state.quadrantFromPoint(state.getRobotPosition(myTeam,
				myIdentifier));
	}

	/**
	 * Get the most recent calculated state of <b>this</b>
	 * 
	 * @return
	 */
	public int getState() {
		return this.myState;
	}

	public int getPrevState() {
		return this.prevState;
	}

	/**
	 * Set <b>this</b> to have a new state
	 * 
	 * @param newState
	 */
	public void setState(int newState) {
		this.prevState = this.myState;
		this.myState = newState;
	}

	/**
	 * Getter method for the sub-state of the robot.
	 * 
	 * @return
	 */
	public int getSubState() {
		return this.defendSubState;
	}

	/**
	 * Setter method for the sub-state of the robot.
	 */
	public void setSubState(int s) {
		this.defendSubState = s;
	}

	/**
	 * Getter method for the kick sub-state of the robot.
	 * 
	 * @return
	 */
	public int getKickSubState() {
		return this.kickSubState;
	}

	/**
	 * Returns a list of quadrant vertices given a quadrant. Essentially just
	 * takes the pitch boundary vertices and returns a subset of them, depending
	 * on the quadrant requested.
	 * 
	 * @param q
	 * @return
	 */
	public LinkedList<Point2> getQuadrantVertices(int q) {
		ArrayList<Point2> pts = state.getPitch().getArrayListOfPoints();
		LinkedList<Point2> vertices = new LinkedList<Point2>();
		if (q == 1) {
			vertices.add(pts.get(1));
			vertices.add(pts.get(2));
			vertices.add(pts.get(11));
			vertices.add(pts.get(12));
			vertices.add(pts.get(13));
			vertices.add(pts.get(14));
		} else if (q == 2) {
			vertices.add(pts.get(2));
			vertices.add(pts.get(3));
			vertices.add(pts.get(10));
			vertices.add(pts.get(11));
		} else if (q == 3) {
			vertices.add(pts.get(3));
			vertices.add(pts.get(4));
			vertices.add(pts.get(9));
			vertices.add(pts.get(10));
		} else if (q == 4) {
			vertices.add(pts.get(4));
			vertices.add(pts.get(5));
			vertices.add(pts.get(6));
			vertices.add(pts.get(7));
			vertices.add(pts.get(8));
			vertices.add(pts.get(9));
		}
		return vertices;
	}

	/**
	 * Returns the vertices of a given quadrant. If the quadrant is a defender
	 * quadrant (edge), it will expand the outer ones.
	 * 
	 * @param q
	 * @return
	 */
	public LinkedList<Point2> getQuadrantVerticesWide(int q) {
		ArrayList<Point2> pts = state.getPitch().getArrayListOfPoints();
		LinkedList<Point2> vertices = new LinkedList<Point2>();
		Point2 expander = new Point2(50, 0);
		if (q == 1) {
			vertices.add(pts.get(1).sub(expander));
			vertices.add(pts.get(2));
			vertices.add(pts.get(11));
			vertices.add(pts.get(12).sub(expander));
			vertices.add(pts.get(13).sub(expander));
			vertices.add(pts.get(14).sub(expander));
		} else if (q == 4) {
			vertices.add(pts.get(4));
			vertices.add(pts.get(5).add(expander));
			vertices.add(pts.get(6).add(expander));
			vertices.add(pts.get(7).add(expander));
			vertices.add(pts.get(8).add(expander));
			vertices.add(pts.get(9));
		} else {
			return getQuadrantVertices(q);
		}
		return vertices;
	}

	/**
	 * Method for checking if <b>this</b> is near the boundary. It does this by
	 * fetching the vertices of its quadrant, contracting them, and then
	 * checking if outwith the hull of this contracted set.
	 * 
	 * @return
	 */
	public boolean nearBoundary() {

		// Get instantaneous quadrant
		int q = getMyQuadrant();

		// Perform sanity check
		if (lastQuadrant != 0 && lastQuadrant != q) {
			System.err.println("Robot Quadrant changed!");
		}

		// Update lastQuadrant flag
		lastQuadrant = q;

		Point2 pos = state.getRobotPosition(myTeam, myIdentifier);
		LinkedList<Point2> vertices = getQuadrantVerticesWide(q);

		if (vertices.size() > 2) {
			return !Alg.inMinorHullWeighted(new LinkedList<Point2>(vertices),
					HULL_OFFSET, pos, 1.0, 0.05);
		} else {
			return false;
		}
	}
}
