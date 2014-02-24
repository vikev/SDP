package sdp.pc.milestones;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import sdp.pc.common.ChooseRobot;
import sdp.pc.common.Constants;
import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;

/**
 * This class contains a main method for executing the attacker perspective in
 * Milestone 3. The implementation will order the robot to navigate to the ball
 * from an offensive angle and shoot for the defending goal.
 * 
 * To run M3att, first pair the machine/robot bluetooth connection. Then
 * navigate to /SDP/trunk/PC/PCControl/ in a terminal and execute ant with the
 * command 'ant'; select defender ('d') during ant build. Run
 * milestone3att.java; select defender again from eclipse ('d'). Calibrate the
 * vision feed by clicking borders.
 * 
 * Note: This class should be re-implemented to use ChooseRobot and TCPClient
 * like code instead of Driver.
 * 
 * @author s1117764
 * 
 */
public class Milestone3att {
	private static int SAFE_ANGLE = 5, SAFE_DIST = 8;

	private static WorldState state = new WorldState();

	/**
	 * Main method for m3att. Performs a kick on a stationary ball and exits.
	 * 
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {

		new Vision(state);
		Thread.sleep(3000);
		final TCPClient conn = new TCPClient(ChooseRobot.dialog());
		final Driver driver = new Driver(conn);

		try {
			driver.stop();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Thread.sleep(500);

		// Shutdown hook so one does not have to re-run 'ant' every time
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Exiting and stopping the robot");
				try {
					driver.stop();
					conn.closeConnection();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Should have stopped by now.");
			}
		});
		Thread.sleep(500);
		JFrame frame = new JFrame("Kick the ball...");
		frame.setBounds(600, 200, 300, 150);
		JButton start = new JButton();
		start.setText("Start");
		start.addActionListener(new ActionListener() {

			/*
			 * On click, reset the local calibration state for the absolute
			 * borders, causing the Vision click listener to look for (first) a
			 * top-left click.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					kickStationaryBall(driver);
					System.out.println("Finished attempt to kick the ball.");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		frame.add(start);
		frame.show();
	}

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
	public static void kickStationaryBall(Driver driver) throws Exception {
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
							+ SAFE_DIST;
					System.out.println(newy);
					faceAndGoTo(driver, new Point2(robotPosition.x, newy));
				} else {
					int newy = robotPosition.y - Constants.ATTACKER_LENGTH
							- SAFE_DIST;
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
		while (!turnTo(state, driver, ballPosition)) {
			Thread.sleep(100);
			while (!turnTo(state, driver, ballPosition)) {
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
	 * Faces and travels to the designated point.
	 */
	public static void faceAndGoTo(Driver driver, Point2 target)
			throws InterruptedException, Exception {
		System.out.println("Attempting to face: " + target);
		while (!turnTo(state, driver, target)) {
			Thread.sleep(50);
		}
		driver.stop();
		while (!(traveltoPoint(driver, target.getX(), target.getY()))) {
			while (!turnTo(state, driver, target)) {
				Thread.sleep(50);
			}
			driver.stop();
			Thread.sleep(50);
		}
		driver.stop();
	}

	/**
	 * Chooses a point within the target goal for the ball to be kicked towards
	 * (auxiliary method) Currently only picks the target goal's centre point
	 * 
	 * @param state
	 * @return
	 */
	public static Point2 setBallTarget() {
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
	 * Calculates an intermediate point for the robot to travel to that is
	 * between the robot and the approach point.(auxiliary method) Assumes that
	 * Y coordinates have already been inverted.
	 * 
	 * @param ballPosition
	 *            , targetPoint
	 * @param ballTargetPoint
	 * @return
	 */
	public static Point2 calculateIntermediatePoint(Point2 ballPosition,
			Point2 ballTargetPoint) {
		int diffX = Math.abs(ballTargetPoint.getX() - ballPosition.getX());
		int newEndPointX = 0;
		int intermediateX = 0;
		double cutOnYAxis;
		if (state.getDirection() == Constants.DIRECTION_RIGHT) {
			newEndPointX = ballTargetPoint.getX() + 2 * diffX;
			intermediateX = ballPosition.getX() - SAFE_DIST;
		}
		if (state.getDirection() == Constants.DIRECTION_LEFT) {
			newEndPointX = ballTargetPoint.getX() - 2 * diffX;
			intermediateX = ballPosition.getX() + SAFE_DIST;
		}
		// Point2 endPoint = new Point2(newEndPointX, ballTargetPoint.getY());
		double gradient = calculateGradient(newEndPointX,
				(ballTargetPoint.getY()), ballPosition.getX(),
				ballPosition.getY());
		cutOnYAxis = (-1)
				* (gradient * ballPosition.getX() + ballPosition.getY()); // result
																			// should
																			// always
																			// be
																			// <=0
		int intermediateY = (int) (Math.floor(gradient * intermediateX) + cutOnYAxis);
		Point2 intermediatePoint = new Point2(intermediateX, intermediateY
				* (-1)); // Invert Y so that it is positive
		return intermediatePoint;
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
	public static boolean traveltoPoint(Driver driver, int targetX, int targetY)
			throws Exception {
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
		while (Math.abs(robotFacing - initialOrientation) < SAFE_ANGLE) {
			if (Math.abs(robotPos.getX() - targetX) < SAFE_DIST
					&& (Math.abs(robotPos.getY() - targetY) < SAFE_DIST)) {
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

	// Duplicate code from Milestone3def below

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
			return 80.0;
		} else if (rotateBy > epsilon) {
			return 30.0;
		} else {
			return 1.0;
		}
	}

	public static boolean assertFacing(WorldState state, Driver driver,
			double deg, double epsilon) {
		double rotateBy = normalizeToBiDirection(state.getRobotFacing(
				state.getOurColor(), state.getDirection())
				- deg);
		try {
			double speed = getRotateSpeed(rotateBy, epsilon);
			if (rotateBy > epsilon) {
				// TODO: cast to int was added. Check if that breaks anything
				driver.turnLeft((int) speed);
			} else if (rotateBy < -epsilon) {
				// TODO: cast to int was added. Check if that breaks anything
				driver.turnRight((int) speed);
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
		Point2 robLoc = state.getRobotPosition(state.getOurColor(),
				state.getDirection());
		while (robLoc.distance(to) >= epsilon) {
			// System.out.println(robLoc.distance(to)-epsilon);
			driver.forward(100);
			Thread.sleep(100);
			robLoc = state.getRobotPosition(state.getOurColor(),
					state.getDirection());
		}
		driver.stop();
		return true;
	}

	public static boolean turnTo(WorldState state, Driver driver, Point2 to) {
		double ang = normalizeToUnitDegrees(state.getRobotPosition(
				state.getOurColor(), state.getDirection()).angleTo(to));
		;
		if (assertFacing(state, driver, ang, SAFE_ANGLE)) {
			return true;
		}
		return false;
	}
}
