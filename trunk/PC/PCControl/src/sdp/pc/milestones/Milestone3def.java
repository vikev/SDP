package sdp.pc.milestones;

import javax.swing.SwingUtilities;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import sdp.pc.common.ChooseRobot;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;

/**
 * Static class for executing milestone 3 from a defending perspective. To run
 * M3def:
 * 
 * <ol>
 * <li>Ensure your robot and machine are paired via bluetooth</li>
 * <li>Run 'ant' via '/SDP/trunk/PC/PCControl/'</li>
 * <li>Follow the instructions in ant, choosing 'd' or 'a'</li>
 * <li>Run Milestone3def as a java application, choosing the same robot 'd' or
 * 'a' at the prompt</li>
 * </ol>
 */
public class Milestone3def {

	/**
	 * An instance of WorldState used by M3def
	 */
	private static WorldState state = new WorldState();

	/**
	 * Minimum ball speed for the robot to consider the ball as approaching the
	 * goal
	 */
	private static final double BALL_SPEED_THRESHOLD = 10.0;

	private static final double PERIOD = (1.0 / 5.0 * 1000.0);

	// Yellow = Team 0; Blue = Team 1
	// Robot on the left - 0; robot on the right - 1
	private static int DEF_TEAM = 0, ATT_TEAM = 0, ATT_ROBOT = 0,
			DEF_ROBOT = 1, SAFE_ANGLE = 5, SAFE_DIS = 1000;

	private static double NEAR_EPSILON = 30;
	private static double NEAR_EPSILON_DIST = 10;
	private static double SAFE_DIST_FROM_GOAL = 30;

	/**
	 * Main method which executes M3def
	 */
	public static void main(String[] args) throws Exception {
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
		final TCPClient conn = new TCPClient(ChooseRobot.dialog());
		final Driver driver = new Driver(conn);

		// Shutdown hook so one does not have to re-run 'ant' every time
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// System.out.println("Exiting and stopping the robot");
				try {
					driver.stop();
					conn.closeConnection();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// System.out.println("Should have stopped by now.");
			}
		});
		Thread.sleep(500);

		// Here is the FSM behaviour of the system
		while (true) {
			// M3 should be a finite state machine that constantly loops,
			// executing the necessary job. User input should not be necessary.

			// FSM:
			// * Assert near goal-line,
			// * Assert perpendicular,
			// * if the ball is moving with sufficient velocity:
			// - block the ball
			// * else:
			// - - if the attacking robot has a hat:
			// * * cut off the attacking robot w.r.t. the goal
			// - - else:
			// * * cut off the ball w.r.t the goal
			if (assertNearGoalLine(state, driver, NEAR_EPSILON_DIST)) {
				if (assertPerpendicular(state, driver)) {
					break;
					// System.out.println("forward");
					// // if (state.getBallSpeed() > BALL_SPEED_THRESHOLD) {
					// // System.out.println("err");
					// // System.out.println("Defending moving ball");
					// // defendBall(state, driver, state.getBallPosition(),
					// state.getBallFacing());
					// // } else {
					// Point2 robotPosition = state.getRobotPosition(ATT_TEAM,
					// ATT_ROBOT);
					// double robotFacing = state.getRobotFacing(ATT_TEAM,
					// ATT_ROBOT);
					// if (!robotPosition.equals(Point2.EMPTY)) {
					// //System.out.println("Defending the robot");
					// defendBall(state, driver, robotPosition, robotFacing);
					// } else {
					// //System.out.println("Defending not moving ball");
					// defendIfNoAttacker(state, driver);
					// }
					// // }
				}
			}

			// Delay a moment to avoid a TCP overflow
			Thread.sleep((int) PERIOD);
		}

		while (true) {

		}
	}

	public static void defendBall(WorldState state, Driver driver,
			Point2 position, double facing) throws Exception {
		Point2 robotPosition = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);

		// Get predicted ball position (Y value) when it will come to
		// defender's side
		// Point2 predBallPos = FutureBall.estimateBallPositionWhen(
		// position, facing, robotPosition, DEF_ROBOT);
		Point2 predBallPos = state.getEstimatedStopPoint();

		System.out.println("Pred pos: " + predBallPos);
		// Move robot to this position
		if (!predBallPos.equals(Point2.EMPTY)) {
			goTo(state, driver, new Point2(robotPosition.getX(), 288));
		}
	}

	public static void defendIfNoAttacker(WorldState state, Driver driver)
			throws Exception {
		Point2 robotPosition = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);
		Point2 ballPosition = state.getBallPosition();

		// Move robot to this position
		goTo(state, driver,
				new Point2(robotPosition.getX(), ballPosition.getY()));
	}

	/**
	 * Returns an angle ang in degrees on [0,360)
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
	 * Returns an angle ang in degrees on [-180,180)
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

	private static double getRotateSpeed(double rotateBy, double epsilon) {
		rotateBy = Math.abs(rotateBy);
		if (rotateBy > 75.0) {
			return 200.0;
		} else if (rotateBy > 25.0) {
			return 100.0;
		} else if (rotateBy > epsilon) {
			return 30.0;
		} else {
			return 1.0;
		}
	}

	public static boolean assertFacing(WorldState state, Driver driver,
			double deg, double epsilon) {
		double rotateBy = normalizeToBiDirection(state.getRobotFacing(DEF_TEAM,
				DEF_ROBOT) - deg);
		try {
			double speed = getRotateSpeed(rotateBy, epsilon);
			if (rotateBy > epsilon) {
				driver.turnLeft(speed);
			} else if (rotateBy < -epsilon) {
				driver.turnRight(speed);
			} else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean assertNear(WorldState state, Driver driver,
			Point2 to, double epsilon) throws Exception {
		Point2 robLoc = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);
		if (robLoc.distance(to) < epsilon) {
			return true;
		}
		driver.forward(300);
		return false;
	}

	public static boolean turnTo(WorldState state, Driver driver, Point2 to) {
		double ang = normalizeToUnitDegrees(state.getRobotPosition(DEF_TEAM,
				DEF_ROBOT).angleTo(to));
		;
		if (assertFacing(state, driver, ang, SAFE_ANGLE)) {
			return true;
		}
		return false;
	}

	public static boolean goTo(WorldState state, Driver driver, Point2 to)
			throws Exception {
		if (assertNear(state, driver, to, NEAR_EPSILON_DIST)) {
			return true;
		}
		return false;
	}

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
					if (goTo(state, driver, goal_centre)) {
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
		
		 double diff = normalizeToBiDirection(normalizeToBiDirection(face -
		 270.0) - normalizeToBiDirection(face - 90.0));
		 
		if (diff > 0) {
			target = 270.0;
		} else {
			target = 90.0;
		}

		// Do it
		if (assertFacing(state, driver, target, SAFE_ANGLE)) {
			driver.stop();
			return true;
		}
		return false;
	}

}
