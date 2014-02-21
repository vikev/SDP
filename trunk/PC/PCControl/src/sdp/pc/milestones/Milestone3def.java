package sdp.pc.milestones;

import javax.swing.SwingUtilities;

import sdp.pc.common.ChooseRobot;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

/**
 * Static class for executing milestone 3 from a defending perspective. To run
 * M3def:
 * 
 * <ol>
 * <li>Set DEF_TEAM and DEF_ROBOT depending to the desired values. Set
 * GOAL_OFFSET to the desired x-offset from the target goal (use a negative
 * number if defending right goal)</li>
 * <li>Run 'ant' via '/SDP/trunk/PC/PCControl/'</li>
 * <li>Follow the instructions in ant, choosing 'd' or 'a'</li>
 * <li>Run Milestone3def as a java application, choosing the same robot 'd' or
 * 'a' at the prompt</li>
 * </ol>
 */
public class Milestone3def {

	/**
	 * The size of the extra buffer a defender can travel beyond the goal mouth,
	 * in pixels.
	 */
	private static final int BETWEEN_GOALS_EPSILON = 3;

	/**
	 * The desired offset from the goal centre for the robot to initialise
	 * itself to, in pixels. Use a negative value if defending the right-hand
	 * goal.
	 */
	private static final int GOAL_OFFSET = 20;

	/**
	 * An instance of WorldState used by M3def
	 */
	private static WorldState state = new WorldState();

	/**
	 * Minimum ball speed for the robot to consider the ball as approaching the
	 * goal in pixels per second.
	 */
	private static final double BALL_SPEED_THRESHOLD = 50.0;

	/**
	 * The period at which the main method executes and sends commands to the
	 * robot, in milliseconds. A value of 1/7*1000 corresponds to 7 times per
	 * second. Avoid sending too many commands per second as the TCP/bluetooth
	 * buffer can "overflow"
	 */
	private static final double PERIOD = (1.0 / 7.0 * 1000.0);

	/**
	 * A constant representing our team (defending team). 0 refers to team
	 * Yellow and 1 refers to team Blue.
	 * 
	 * TODO: This should be abstracted.
	 */
	private static final int DEF_TEAM = 0;

	/**
	 * A constant for our robot. 0 would be on the left, 1 on the right.
	 * 
	 * TODO: This should be abstracted.
	 */
	private static final int DEF_ROBOT = 0;

	/**
	 * A useful epsilon value for asserting our robots facing angle. We can
	 * adjust how precise we want it to be to the true value with this. A higher
	 * number will achieve the desired angle faster. Value is in degrees.
	 */
	private static final int FACING_EPSILON = 5;

	/**
	 * Similar to FACING_EPSILON, this value defines how close, in pixels, the
	 * robot should be to a target point before it is considered done. A higher
	 * value means the robot will achieve its goal faster but with less
	 * precision.
	 */
	private static double NEAR_EPSILON_DIST = 10;

	/**
	 * How far from the goalmouth centre the robot should travel before
	 * returning, in pixels.
	 */
	private static double SAFE_DIST_FROM_GOAL = 30;

	/**
	 * Main method which executes M3def
	 */
	public static void main(String[] args) throws Exception {

		// Start the vision system
		Thread.sleep(2000);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new Vision(state);
				} catch (V4L4JException e) {
					e.printStackTrace();
				}
			}
		});

		// Connect to a robot
		final TCPClient conn = new TCPClient(ChooseRobot.dialog());
		final Driver driver = new Driver(conn);

		// Shutdown hook so one does not have to re-run 'ant' every time
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Exiting and stopping the robot");
				try {
					driver.stop();
					conn.closeConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("Should have stopped by now.");
			}
		});
		Thread.sleep(500);

		// The following section was disabled to adhere to the (last minute)
		// milestone rules:

		// // Here is the FSM behaviour of the system
		// while (true) {
		// // M3 should be a finite state machine that constantly loops,
		// // executing the necessary job. User input should not be necessary.
		//
		// // FSM:
		// // * Assert near goal-line,
		// // * Assert perpendicular,
		// // * if the ball is moving with sufficient velocity:
		// // - block the ball
		// // * else:
		// // - - if the attacking robot has a hat:
		// // * * cut off the attacking robot w.r.t. the goal
		// // - - else:
		// // * * cut off the ball w.r.t the goal
		// if (assertNearGoalLine(state, driver, NEAR_EPSILON_DIST)) {
		// if (assertPerpendicular(state, driver)) {
		// break;
		// }
		// }
		//
		// // Delay a moment to avoid a TCP overflow
		// Thread.sleep((int) PERIOD);
		// }

		while (true) {

			// If the ball is moving fast enough, defend it.
			if (state.getBallSpeed() > BALL_SPEED_THRESHOLD) {
				defendBall(state, driver);
			} else {

				// The following code was disabled to adhere to the (last
				// minute) milestone rules:

				// Point2 robotPosition = state.getRobotPosition(ATT_TEAM,
				// ATT_ROBOT);
				// double robotFacing = state.getRobotFacing(ATT_TEAM,
				// ATT_ROBOT);
				// if(assertPerpendicular(state, driver)){
				// defendIfNoAttacker(state, driver);
				// }
				// if (!robotPosition.equals(Point2.EMPTY)) {
				// defendRobot(state, driver, robotPosition, robotFacing);
				// } else {
				// defendIfNoAttacker(state, driver);
				// }
			}

			Thread.sleep((int) PERIOD);
		}
	}

	/**
	 * Synchronous method which performs (briefly) the goal of defending the
	 * ball. It checks the predicted stop location of the ball and moves to its
	 * Y coordinate by going forwards or backwards.
	 */
	public static void defendBall(WorldState state, Driver driver)
			throws Exception {

		// Get predicted ball stop point
		Point2 predBallPos = state.getFutureData().getResult();

		// If that position exists, go to its Y coordinate, otherwise stop.
		if (!predBallPos.equals(Point2.EMPTY)) {
			defendTo(state, driver, predBallPos.getY(), NEAR_EPSILON_DIST);
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
	public static void defendRobot(WorldState state, Driver driver,
			Point2 position, double facing) throws Exception {

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
				position).getResult();
		// Move robot to this position
		if (!predBallPos.equals(Point2.EMPTY)) {
			defendTo(state, driver, predBallPos.getY(), NEAR_EPSILON_DIST);
		} else {
			driver.stop();
		}
	}

	/**
	 * Returns true if a given Y coordinate is between the specified goalmouth
	 * endpoints, with some epsilon value.
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
	 * backward to cut off the estimated ball postion's Y coordinte. The method
	 * is synchronous, and therefore must be called until it returns true if you
	 * expect it to be finished.
	 */
	private static boolean defendTo(WorldState state2, Driver driver, int y,
			double eps) throws Exception {

		double botFacing = state.getRobotFacing(DEF_TEAM, DEF_ROBOT);
		Point2 botPosition = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);
		int botX = botPosition.getX();
		double angleToBall = botPosition.angleTo(new Point2(botX, y));
		boolean between = betweenGoals(y, DEF_ROBOT, BETWEEN_GOALS_EPSILON);
		int estStopY = state.getFutureData().getResult().getY();

		// Compare robot facing with angle to ball
		double diff = normalizeToBiDirection(botFacing - angleToBall);

		// If the robot is far enough from the target, and between the goals:
		if (botPosition.distance(new Point2(botX, estStopY)) > eps && between) {

			// Assert the robot is near the target, by going forward or backward
			if (Math.abs(diff) > 90) {
				assertNearReverse(state, driver, new Point2(botX, estStopY),
						NEAR_EPSILON_DIST);
			} else {
				assertNearForward(state, driver, new Point2(botX, estStopY),
						NEAR_EPSILON_DIST);
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
	public static void defendIfNoAttacker(WorldState state, Driver driver)
			throws Exception {
		Point2 robotPosition = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);
		Point2 ballPosition = state.getBallPosition();

		double q;
		if (DEF_ROBOT == 0) {
			q = state.getLeftGoalCentre().getY();
		} else {
			q = state.getRightGoalCentre().getY();
		}

		q += ballPosition.getY();
		q /= 2;

		// Move robot to this position
		defendTo(state, driver, (int) q, NEAR_EPSILON_DIST);
	}

	/**
	 * Returns an angle ang in degrees on [0,360).
	 * 
	 * TODO: Should be abstracted to an Angle class, I guess.
	 * 
	 * @param ang
	 *            angle in degrees
	 * @return ang on [0,360)
	 */
	private static double normalizeToUnitDegrees(double ang) {
		while (ang < 0.0) {
			ang += 360.0;
		}
		while (ang >= 360.0) {
			ang -= 360.0;
		}
		return ang;
	}

	/**
	 * Returns an angle ang in degrees on [-180,180), useful for comparing
	 * angles.
	 * 
	 * TODO: Should be abstracted to an Angle class, I guess.
	 * 
	 * @param ang
	 *            angle in degrees
	 * @return ang on [-180, 180)
	 */
	private static double normalizeToBiDirection(double ang) {
		while (ang < -180.0) {
			ang += 360.0;
		}
		while (ang >= 180.0) {
			ang -= 360.0;
		}
		return ang;
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
	public static boolean assertFacing(WorldState state, Driver driver,
			double deg, double epsilon) {
		double rotateBy = normalizeToBiDirection(state.getRobotFacing(DEF_TEAM,
				DEF_ROBOT) - deg);
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
	 * Synchronus and must be called continuously.
	 */
	public static boolean assertNearForward(WorldState state, Driver driver,
			Point2 to, double epsilon) throws Exception {
		Point2 robLoc = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);
		if (robLoc.distance(to) < epsilon) {
			return true;
		}
		double speed = getMoveSpeed(robLoc.distance(to), NEAR_EPSILON_DIST);
		driver.forward(speed);
		return false;
	}

	/**
	 * Makes the robot go backward as long as it's outwith <b>to</b> with
	 * windowsize epsilon. We therefore assume the robot is perpendicular.
	 * Synchronous and must be called continuously.
	 */
	public static boolean assertNearReverse(WorldState state, Driver driver,
			Point2 to, double epsilon) throws Exception {
		Point2 robLoc = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);
		if (robLoc.distance(to) < epsilon) {
			return true;
		}
		double speed = getMoveSpeed(robLoc.distance(to), NEAR_EPSILON_DIST);
		driver.backward(speed);
		return false;
	}

	/**
	 * Similar to getRotateSpeed, gets a reasonable move speed for the robot
	 * depending how far it is from the target.
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
	public static boolean turnTo(WorldState state, Driver driver, Point2 to) {
		double ang = normalizeToUnitDegrees(state.getRobotPosition(DEF_TEAM,
				DEF_ROBOT).angleTo(to));
		if (assertFacing(state, driver, ang, FACING_EPSILON)) {
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
	public static boolean goTo(WorldState state, Driver driver, Point2 to,
			double eps) throws Exception {
		if (assertNearForward(state, driver, to, eps)) {
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
	public static boolean assertNearGoalLine(WorldState state, Driver driver,
			double eps) {
		try {
			Point2 botPos = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);
			Point2 goal_centre;
			if (DEF_ROBOT == 0) {
				goal_centre = state.getLeftGoalCentre();
			} else {
				goal_centre = state.getRightGoalCentre();
			}
			if (botPos.distance(goal_centre) > SAFE_DIST_FROM_GOAL) {
				if (turnTo(state, driver, goal_centre)) {
					if (goTo(state, driver, new Point2(goal_centre.getX()
							+ GOAL_OFFSET, goal_centre.getY()), eps)) {
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
	public static boolean assertPerpendicular(WorldState state, Driver driver)
			throws Exception {

		// Calculate which angle is the closest perpendicular one
		double target;
		double face = state.getRobotFacing(DEF_TEAM, DEF_ROBOT);

		double a = normalizeToBiDirection(face - 90.0);

		if (Math.abs(a) < 90.0) {
			target = 90.0;
		} else {
			target = 270.0;
		}

		// Do it
		if (assertFacing(state, driver, target, FACING_EPSILON)) {
			driver.stop();
			return true;
		}
		return false;
	}

}
