package sdp.pc.milestones;

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
	
	private static int  SAFE_ANGLE = 6, SAFE_DIST = 10;

	private static WorldState state = new WorldState();

	/**
	 * Main method for m3att. Performs a kick on a stationary ball and exits.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		/*
		 * Point2 ballPosition = new Point2(388,312); //calculateAngle method
		 * testing Point2 robotPosition = new Point2(396,362); Point2
		 * targetPoint = new Point2(70,234); int[] vectorA =
		 * {targetPoint.getX()-ballPosition.getX(),(targetPoint.getY()*(-1) -
		 * ballPosition.getY()*(-1))}; int[] vectorB =
		 * {robotPosition.getX()-ballPosition.getX(),(robotPosition.getY()*(-1)
		 * - ballPosition.getY()*(-1))};
		 * System.out.println("Vector A Components : " + vectorA[0] + " " +
		 * vectorA[1]); System.out.println("Vector B Components : " + vectorB[0]
		 * + " " + vectorB[1]); System.out.println("Angle between vectors is : "
		 * + calculateAngle(vectorA, vectorB));
		 */new Vision(state);
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
		
		
		kickStationaryBall(driver);
		System.out.println("Finished attempting to kick ball");
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
		Point2 robotPosition = state.getRobotPosition(state.getOurColor(), state.getDirection());
		Point2 ballPosition = state.getBallPosition();
		Point2 targetPoint = setBallTarget();
		boolean succesfulTravel = false;
		// System.out.println("Robot Position :" + robotPosition);
		// System.out.println("Ball Position :" + ballPosition);
		// System.out.println("Ball Target Point :" + targetPoint);

		if (targetPoint.getX() == 0 && targetPoint.getY() == 0) {
			System.out
					.println("Could not assign ball target point within target goal");
			return; // could not set target point for ball and therefore cannot
					// take shot
		}

		int[] vectorBalltoTargetPoint = { // Y coordinates must be inverted
		(ballPosition.getX() - targetPoint.getX()),
				(ballPosition.getY() * (-1)) - (targetPoint.getY() * (-1)) };
		int[] vectorBalltoSelectedRobot = {
				(ballPosition.getX() - robotPosition.getX()),
				(ballPosition.getY() * (-1)) - -(robotPosition.getY() * (-1)) };

		double angleBetween = calculateAngle(vectorBalltoTargetPoint,
				vectorBalltoSelectedRobot); 
		System.out.println("Angle between bot, ball and goal : " + angleBetween);
		// check if the ball will obstruct a direct path to the approach
		// point move to a point where a direct path can be taken to the
		// approach point
		Point2 approachPoint = calculateApproachPoint(ballPosition,
				targetPoint, state.getDirection());
		System.out.println("Ball point: " + ballPosition);
		System.out.println("Assigned approach point: " + approachPoint);
		if ((approachPoint.getX() == 0) && (approachPoint.getY() == 0)) {
			System.out.println("Could not assign Approach Point");
			return;
		}
		//if ((angleBetween > 90)) {
		if (true) {
			// Attempt to navigate to the approach point.
			//facePoint(state, driver, approachPoint.getX(), approachPoint.getY());
			while (!Milestone3def.turnTo(state, driver, approachPoint)) {
				Thread.sleep(200);
			}
			driver.stop();
			while (!(succesfulTravel = traveltoPoint(driver,
					approachPoint.getX(), approachPoint.getY(), 0))) {
				while (!Milestone3def.turnTo(state, driver, approachPoint)) {
					Thread.sleep(200);
				}
				driver.stop();
			}
		} else {/*
			Point2 intermediate_point = calculateIntermediatePoint(ballPosition, targetPoint);
			//facePoint(driver, intermediate_point.getX(), intermediate_point.getY());
			while (!(succesfulTravel = traveltoPoint(state, driver,
					intermediate_point.getX(), intermediate_point.getY()))) {
				//facePoint(driver, intermediate_point.getX(),
						intermediate_point.getY();
			}
			// Attempt to navigate to the approach point.
			//facePoint(driver, approachPoint.getX(), approachPoint.getY());
			while (!(succesfulTravel = traveltoPoint(state, driver,
					approachPoint.getX(), approachPoint.getY()))) {
				//facePoint(driver, approachPoint.getX(),
						//approachPoint.getY());
			}*/
			return;
		}
		while (!Milestone3def.turnTo(state, driver, ballPosition)) {
			Thread.sleep(200);
		}
		driver.stop();
		while (!(succesfulTravel = traveltoPoint(driver,
				targetPoint.getX(), targetPoint.getY(), 1))) {
			facePoint(state, driver, targetPoint.getX(), targetPoint.getY());
		}
		driver.forward(400);
		Thread.sleep(300);
		driver.stop();
		driver.kick(900);
		System.out.println("KICK!");
	}

	/**
	 * Chooses a point within the target goal for the ball to be kicked towards
	 * (auxiliary method) Currently only picks the target goal's centre point
	 * 
	 * @param state
	 * @return
	 */
	public static Point2 setBallTarget() {
		if (state.getDirection() == Constants.DIRECTION_RIGHT) // if target goal is the right hand goal
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
				/ (vectorAMagnitude * vectorBMagnitude)) *180/Math.PI;
		//System.out.println("Vector A Magnitude : " + vectorAMagnitude);
		//System.out.println("Vector B Magnitude : " + vectorBMagnitude);
		//System.out.println("Dot product : " + dotproduct);
		//System.out.println("Angle (in degrees) : " + angleDegrees);
		return angleDegrees;
	}
	
	/**
	 * Calculates an intermediate point for the robot to travel to that is between
	 * the robot and the approach point.(auxiliary method)
	 * 
	 * @param ballPosition, targetPoint
	 * @param ballTargetPoint
	 * @return
	 */	
	public static Point2 calculateIntermediatePoint(Point2 ballPosition, Point2 ballTargetPoint){
		int diffX = Math.abs(ballTargetPoint.getX() - ballPosition.getX());
		int newEndPointX = 0;
		int intermediateX = 0;
		if (state.getDirection() == Constants.DIRECTION_RIGHT){
			newEndPointX = ballTargetPoint.getX() + 2*diffX;
			intermediateX = ballPosition.getX() - SAFE_DIST;
		}if (state.getDirection() == Constants.DIRECTION_LEFT){
			newEndPointX = ballTargetPoint.getX() - 2*diffX;
			intermediateX = ballPosition.getX() + SAFE_DIST;
		}
		Point2 endPoint = new Point2(newEndPointX, ballTargetPoint.getY());
		double gradient = calculateGradient(newEndPointX, (ballTargetPoint.getY()*(-1)), ballPosition.getX(), (ballPosition.getY()*(-1))); 
		int intermediateY = (int) Math.floor((-1)*gradient*intermediateX); 
		Point2 intermediatePoint = new Point2(intermediateX, intermediateY);
		return intermediatePoint;
	}
	
	/**
	 * Calculates the gradient between two points (auxiliary
	 * method)
	 * 
	 * @param XcoordinatePoint1
	 * @param YcoordinatePoint1
	 * @param XcoordinatePoint2
	 * @param YcoordinatePoint2
	 * @return
	 */
	public static double calculateGradient(int XcoordinatePoint1, int YcoordinatePoint1, int XcoordinatePoint2, int YcoordinatePoint2){
		double diffY = YcoordinatePoint2 - YcoordinatePoint1;
		double diffX = XcoordinatePoint2 - XcoordinatePoint1;
		return (diffY/diffX);
	}

	/**
	 * Calculates the attacking robots approach point to the ball (auxiliary
	 * method)
	 * 
	 * @param ballPosition
	 * @param ballTargetPoint
	 * @param targetGoal
	 * @return
	 */
	public static Point2 calculateApproachPoint(Point2 ballPosition,
			Point2 ballTargePoint, int targetGoal) {
		int approachPointX = 0;
		int approachPointY = 0;
		double gradientLineBalltoGoal = calculateGradient(ballPosition.getX(),(ballPosition.getY() * (-1)), //Y coordinates must be inverted
		ballTargePoint.getX(), (ballTargePoint.getY() * (-1)));
		if (targetGoal == Constants.DIRECTION_LEFT) {
			approachPointX = ballPosition.getX()
					+ Math.max(Constants.ATTACKER_LENGTH,
							Constants.ATTACKER_WIDTH);
		} else if (targetGoal == Constants.DIRECTION_RIGHT) {
			approachPointX = ballPosition.getX()
					- Math.max(Constants.ATTACKER_LENGTH,
							Constants.ATTACKER_WIDTH);
		}
		approachPointY = ballPosition.y;
		//approachPointY = (int) Math.floor(-(gradientLineBalltoGoal * approachPointX) + ballPosition.getY());
		return new Point2(approachPointX, approachPointY);
	}

	/**
	 * Orders the attacking robot to travel forwards until it is reasonably
	 * close to the target point.
	 * If the attacker robots orientation deviates from its initial orientation
	 * by a set amount then the the robot is commanded to stop and the travel is
	 * reported back as unsuccessful. Then the facePoint method needs to be
	 * called externally to correct the robots orientation before this method
	 * should be called again (auxiliary method)
	 * 
	 * @param driver
	 * @param targetX
	 * @param targetY
	 * @param movingTowardsBall
	 * @return
	 */

	public static boolean traveltoPoint(Driver driver,
			int targetX, int targetY, int movingTowardsBall) throws Exception {
		double initialOrientation = state.getRobotFacing(state.getOurColor(), state.getDirection());
		double robotFacing = state.getRobotFacing(state.getOurColor(), state.getDirection());
		Point2 robotPos = state.getRobotPosition(state.getOurColor(), state.getDirection());
		int threshold = 2;
		if (movingTowardsBall == 1){
			threshold = SAFE_DIST;
		}
		if((Math.abs(robotPos.getX() - targetX) > threshold
				&& (Math.abs(robotPos.getY() - targetY) > threshold))){
			driver.forward(50);	//command robot to go forward at set speed
		}
		while (Math.abs(robotPos.getX() - targetX) > threshold
				&& (Math.abs(robotPos.getY() - targetY) > threshold)){
			if (Math.abs(robotFacing - initialOrientation) < SAFE_ANGLE) {
				driver.stop();
				return false;
			}
			Thread.sleep(50);
			//Update position & orientation
			robotPos = state.getRobotPosition(state.getOurColor(), state.getDirection());
			robotFacing = state.getRobotFacing(state.getOurColor(), state.getDirection());
		}
		driver.stop();
		return true;
	}
	
	/**
	 * Rotates the attacking robot to face the given coordinates (auxiliary
	 * method)
	 * 
	 * @param state
	 * @param driver
	 * @param Xcoodinate
	 * @param Ycoordinate
	 * @return
	 */
	public static void facePoint(WorldState state, Driver driver,
			int Xcoordinate, int Ycoordinate) {
		double deltaY, deltaX;
		Point2 robotPosition = state.getRobotPosition(state.getOurColor(), state.getDirection());
		double robotOrientation = state.getRobotFacing(state.getOurColor(), state.getDirection());
		deltaX = Math.abs(robotPosition.getX() - Xcoordinate);
		deltaY = Math.abs(Ycoordinate - robotPosition.getY());
		double targetAngle = 360 - (Math.atan(deltaY / deltaX) * 180/Math.PI);
		double rotateBy = robotOrientation - targetAngle;
		while (Math.abs(rotateBy) > SAFE_ANGLE) {
			try {
				if (rotateBy > 0) {
					if (rotateBy > 180) {
						driver.turnRight(5);
					} else {
						driver.turnLeft(5);
					}
				} else {
					if (-rotateBy > 180) {
						driver.turnLeft(5);
					} else {
						driver.turnRight(5);
					}
				}
				try {
				Thread.sleep(150);
				} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
				}
			} catch (Exception e1) {
				System.out
						.println("Exception encountered while trying to turn to face point "
								+ Xcoordinate + ", " + Ycoordinate);
				e1.printStackTrace();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			robotOrientation = state.getRobotFacing(state.getOurColor(), state.getDirection());
			rotateBy = robotOrientation - targetAngle;
		}
	}

	/*
	 * Pseudo Code for getting the robot to navigate to, and kick, the ball.
	 * Call chooseRobot connect to chosen Robot create Vision instance discern
	 * target goal assign target point within goal (currently only use goal
	 * centre) - separate method create vectors for line ball to goal and for
	 * ball to robot using ball position as (0,0) point of coordinate axis
	 * assign ball's approach point if ball's approach point lies within
	 * selected robots zone then if angle between line connecting ball to the
	 * target goal and the line connecting the ball to the Robot is >90 degrees
	 * and <270 move to the ball's approach point face target goal approach and
	 * kick ball else set way-point 90 degrees or 270 degrees to the line
	 * connecting ball to goal (whichever is closer to the angle between line
	 * connecting ball to the target goal and the line connecting the ball to
	 * the Robot) if way-point is within robots bounding box go to way-point
	 * face and move to the ball's approach point face target goal and approach
	 * and kick ball else Robot is stuck in a corner of its zone and can't kick
	 * the ball towards the goal. display that shot has been deemed impossible
	 * else display that shot has been deemed impossible end
	 * 
	 * Throughout this loop if ball location is changed then loop should start
	 * from the top again after a period of waiting for the ball's reported
	 * position to stabilise
	 */

}
