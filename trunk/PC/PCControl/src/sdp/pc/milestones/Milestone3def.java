package sdp.pc.milestones;

import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.FutureBall;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;
import sdp.pc.common.*;

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
	 * Minimum ball speed for the robot to consider the ball as approaching the
	 * goal
	 */
	private static final double BALL_SPEED_THRESHOLD = 10.0;

	/**
	 * An instance of WorldState used by M3def
	 */
	private static WorldState state = new WorldState();

	// Yellow = Team 0; Blue = Team 1
	private static int DEF_TEAM = 0, ATT_TEAM = 0, ATT_ROBOT = 1,
			DEF_ROBOT = 0, SAFE_ANGLE = 10, SAFE_DIS = 1000;

	/**
	 * Main method which executes M3def
	 */
	public static void main(String[] args) throws Exception {
		Vision vision = new Vision(state);
		Thread.sleep(3000);

		Driver driver = new Driver(new TCPClient(ChooseRobot.dialog()));

		try {
			driver.stop();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Thread.sleep(500);
		while (true) {
			// M3 should be a finite state machine that constantly loops,
			// executing the necessary job. User input should not be necessary.

			// Scanner reader = new Scanner(System.in);
			// System.out.println("Enter the first number");
			// // get user input for a
			// int a = reader.nextInt();
			// if (a == 0) {
			// defendBall(state, vision, driver);
			// System.out.println("Finished attempting to defend the ball");
			// }

			// * Assert perpendicular,
			// * if the ball is moving with sufficient velocity:
			// - block the ball
			// * else:
			// - if the attacking robot has a hat:
			// * cut off the attacking robot w.r.t. the goal
			// - else:
			// * cut off the ball w.r.t the goal
			rotatePerpendicular(state, vision, driver);
			if (state.getBallSpeed() > BALL_SPEED_THRESHOLD) {

			}
		}
	}

	public static void defendBall(WorldState state, Vision vision, Driver driver) {
		Point2 ballPosition = state.getBallPosition();
		System.out.println("Ball is at: " + ballPosition);

		double ballFacing = state.getBallFacing();
		System.out.println("Ball facing: " + ballFacing);

		Point2 robotPosition = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);
		System.out.println("Initially robot is at: " + robotPosition);

		double robotFacing = state.getRobotFacing(DEF_TEAM, DEF_ROBOT);
		System.out.println("Initial robot facing angle: " + robotFacing);

		/**
		 * Two states: Ball is not moving: 1. rotate perpendicular to edges 2.
		 * cut off the direction of attacking robot Ball is moving: 1. cut off
		 * the ball
		 */

		// At the beginning make sure that robot is facing perpendicular to
		// edges
		rotatePerpendicular(state, vision, driver);

		if (state.getBallFacing() == -1) {
			// Get the facing direction of the attacking robot
			Point2 attPosition = state.getRobotPosition(ATT_TEAM, ATT_ROBOT);
			System.out.println("Attacking robot is at: " + attPosition);

			double attFacing = state.getRobotFacing(ATT_TEAM, ATT_ROBOT);
			System.out.println("Attacking robot facing angle: " + attFacing);

			// Get predicted ball position (Y value) when it will come to
			// defender's side
			double predBallPos = FutureBall.estimateBallPositionWhen(
					attPosition, attFacing, robotPosition.getX());

			// Drive robot to this position
			driveRobot(state, vision, driver, predBallPos);
		} else {
			/**
			 * If the ball is already moving - make sure that the robot will cut
			 * off it
			 */
		}
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
		while (ang <= 360.0) {
			ang -= 360.0;
		}
		return ang;
	}

	public static void rotatePerpendicular(WorldState state, Vision vision,
			Driver driver) {

		double robotFacing = state.getRobotFacing(DEF_TEAM, DEF_ROBOT);
		double rotateBy;
		boolean rotateDown = false;

		if (robotFacing > 0 & robotFacing < 180) {
			rotateBy = robotFacing - 90;
			rotateDown = true;
		} else {
			rotateBy = robotFacing - 270;
		}

		while (Math.abs(rotateBy) > SAFE_ANGLE) {
			if (rotateDown) {
				if (robotFacing < 90) {
					try {
						driver.turnRight();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				} else {
					try {
						driver.turnLeft();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			} else {
				if (robotFacing < 270) {
					try {
						driver.turnRight();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				} else {
					try {
						driver.turnLeft();
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}

			// try {
			// //Thread.sleep(100);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			robotFacing = state.getRobotFacing(0, 0);

			if (robotFacing > 0 & robotFacing < 180) {
				rotateBy = robotFacing - 90;
			} else {
				rotateBy = robotFacing - 270;
			}
		}
		try {
			driver.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// double shit = 270 - robotFacing;
		// System.out.println("Turned by: " + shit);
	}

	public static void driveRobot(WorldState state, Vision vision,
			Driver driver, double predBallPos) {
		double robotFacing = state.getRobotFacing(DEF_TEAM, DEF_ROBOT);
		Point2 robotPosition = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);

		while (Math.abs(robotPosition.getY() - predBallPos) > SAFE_DIS) {
			double driveBy = predBallPos - robotPosition.getY();
			System.out.println(driveBy);
			if ((robotFacing > 90 - SAFE_ANGLE)
					& (robotFacing < 90 + SAFE_ANGLE)) {
				if (driveBy < 0) {
					try {
						driver.backward(-driveBy);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						driver.forward(driveBy);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if ((robotFacing > 270 - SAFE_ANGLE)
					& (robotFacing < 270 + SAFE_ANGLE)) {
				if (driveBy < 0) {
					try {
						driver.forward(-driveBy);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						driver.backward(driveBy);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				rotatePerpendicular(state, vision, driver);
			}
			robotPosition = state.getRobotPosition(DEF_TEAM, DEF_ROBOT);
			robotFacing = state.getRobotFacing(DEF_TEAM, DEF_ROBOT);
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
