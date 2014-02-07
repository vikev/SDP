package sdp.pc.milestones;

import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;
import sdp.pc.common.*;

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

	private static WorldState state = new WorldState();

	/**
	 * Main method for m3att. Performs a kick on a stationary ball and exits.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Vision vision = new Vision(state);
		Thread.sleep(3000);
		int chosenRobot = ChooseRobot.dialog();

		Driver driver = new Driver(new TCPClient(chosenRobot));

		try {
			driver.stop();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Thread.sleep(500);
		kickStationaryBall(state, driver, vision, chosenRobot);
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
	 **/
	public static void kickStationaryBall(WorldState state, Driver driver,
			Vision vision, int chosenRobot) throws Exception {
		if (chosenRobot == Constants.ATTACKER)
			chosenRobot = 0;
		if (chosenRobot == Constants.DEFENDER)
			chosenRobot = 1;
		Point2 robotPosition = state.getRobotPosition(0, 0);
		Point2 ballPosition = state.getBallPosition();
		Point2 targetPoint = setBallTarget(state);
		boolean succesfulTravel = false;
		//System.out.println("Robot Position :" + robotPosition);
		//System.out.println("Ball Position :" + ballPosition);
		//System.out.println("Ball Target Point :" + targetPoint);

		if (targetPoint.getX() == 0 && targetPoint.getY() == 0) {
			System.out.println("Could not assign ball target point within target goal");
			return; // could not set target point for ball and therefore cannot
					// take shot
		}
		
		int[] vectorBalltoTargetPoint = {
				(ballPosition.getX() - targetPoint.getX()),
				(ballPosition.getY() - targetPoint.getY()) };
		int[] vectorBalltoSelectedRobot = {
				(ballPosition.getX() - robotPosition.getX()),
				(ballPosition.getY() - robotPosition.getY()) };
		
		int angleBetween = calculateAngle(vectorBalltoTargetPoint, vectorBalltoSelectedRobot); //this assumes angle is positive in the clockwise direction
		// check if the ball will obstruct a direct path to the approach
		// point move to a point where a direct path can be taken to the
		// approach point
		if (angleBetween > 90 && angleBetween < 270){
			Point2 approachPoint = calculateApproachPoint(ballPosition, targetPoint, state.getTargetGoal());
			System.out.println("Assigned approach point: " + approachPoint);
			if ((approachPoint.getX() == 0) && (approachPoint.getY() == 0)){
				System.out.println("Could not assign Approach Point");
				return;
			}
			// Attempt to navigate to the approach point.
			facePoint(state, driver, approachPoint.getX(), approachPoint.getY());
			while (!(succesfulTravel = traveltoPoint(state, driver, approachPoint.getX(), approachPoint.getY()))){
				facePoint(state, driver, approachPoint.getX(), approachPoint.getY());
			}
		}else{
			System.out.print("Have to navigate around ball");
			return;
		}
		facePoint(state, driver, targetPoint.getX(), targetPoint.getY());
		while (!(succesfulTravel = traveltoPoint(state, driver, targetPoint.getX(), targetPoint.getY()))){
			facePoint(state, driver, targetPoint.getX(), targetPoint.getY());
		}
		driver.kick(10);
	}
	
	/**
	 * Chooses a point within the target goal for the ball to be kicked towards (auxillary method)
	 * Currently only picks the target goal's centre point
	 * @param state
	 * @return
	 */
	public static Point2 setBallTarget(WorldState state) {
		if (1 == state.getTargetGoal()) // if target goal is the right hand goal
			return state.getRightGoalCentre();
		if (0 == state.getTargetGoal())
			return state.getLeftGoalCentre();
		return new Point2(0, 0);
	}
	
	/*TODO:
	 * implement method of calculating angle between two vectors
	 * may not be needed depending on the kicker that is chosen 
	*/
	public static int calculateAngle(int[] vectorA, int[] vectorB){
		return 100;
	}
	
	/**
	 * Calculates the attacking robots approach point to the ball (auxillary method)
	 * 
	 * @param ballPosition
	 * @param ballTargetPoint
	 * @param targetGoal
	 * @return
	 */
	public static Point2 calculateApproachPoint(Point2 ballPosition, Point2 ballTargePoint, int targetGoal){
		int approachPointX = 0;
		int approachPointY = 0;
		int gradientLineBalltoGoal = (-(ballTargePoint.getY() - ballPosition.getY())/ (ballTargePoint.getX() - ballPosition.getX()));
		if (targetGoal == Constants.GOAL_LEFT){
			approachPointX = ballPosition.getX() - Math.max(Constants.ATTACKER_LENGTH, Constants.ATTACKER_LENGTH);
		}else if (targetGoal == Constants.GOAL_RIGHT){
			approachPointX = ballPosition.getX() + Math.max(Constants.ATTACKER_LENGTH, Constants.ATTACKER_LENGTH);
		}
		approachPointY = -(gradientLineBalltoGoal*approachPointX);
		return new Point2(approachPointX + ballPosition.getX(), approachPointY + ballPosition.getY());
	}
	
	/**
	 * Orders the attacking robot to go to travel forwards until
	 * it is reasonably close (within 5 pixels) to the target point.
	 * If the robots orientation deviates a set amount then facePoint
	 * is called to correct the robots course (auxillary method) 
	 * @param state
	 * @param driver
	 * @return
	 */
	
	public static boolean traveltoPoint(WorldState state, Driver driver, int targetX, int targetY) throws Exception{
		double initialOrientation = state.getRobotFacing(0, 0);
		if (Math.abs(state.getRobotPosition(0, 0).getX() - targetX) > 5 && (Math.abs(state.getRobotPosition(0, 0).getY() - targetY) > 5));
		driver.forward();
		while (Math.abs(state.getRobotFacing(0, 0) - initialOrientation) < 5){
			if (Math.abs(state.getRobotPosition(0, 0).getX() - targetX) < 5 & (Math.abs(state.getRobotPosition(0, 0).getY() - targetY) < 5)){
				driver.stop();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Rotates the attacking robot to face the given coordinates (auxillary method)
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
		Point2 robotPosition = state.getRobotPosition(0, 0);
		System.out.println("robotPosition");
		System.out.println(robotPosition);
		Point2 ballPosition = state.getBallPosition();
		System.out.println("ball position");
		System.out.println(ballPosition);
		double robotOrientation = state.getRobotFacing(0, 0);
		System.out.println("robotOrientation");
		System.out.println(robotOrientation);
		deltaX = Math.abs(robotPosition.getX() - ballPosition.getX());
		deltaY = Math.abs(ballPosition.getY() - robotPosition.getY());
		double targetAngle = 360 - (Math.atan(deltaY / deltaX) * 360 / (2 * Math.PI));
		double targetAngleDegrees = targetAngle*(180/Math.PI);
		System.out.println("targetAngle");
		System.out.println(targetAngle);
		double rotateBy = robotOrientation - targetAngleDegrees;
		System.out.println("Rotate by:");
		System.out.println(rotateBy);
		while (Math.abs(rotateBy) > 8) {
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
				// try {
				// Thread.sleep(1000);
				// } catch(InterruptedException ex) {
				// Thread.currentThread().interrupt();
				// }
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
			robotOrientation = state.getRobotFacing(0, 0);
			rotateBy = robotOrientation - targetAngle;
		}
	}

	/*
	 * Pseudo Code for getting the robot to navigate to, and kick, the ball.
	 * Call chooseRobot
	 * connect to chosen Robot
	 * create Vision instance 
	 * discern target goal assign target point within goal (currently only use goal centre) - separate method 
	 * create vectors for line ball to goal and for ball to robot using ball position as (0,0) point of coordinate axis
	 * assign ball's approach point if ball's approach point lies within selected robots zone then 
	 * if angle between line connecting ball to the target goal and the line connecting the ball to the Robot is >90 degrees and <270
	 *  	move to the ball's approach point
	 *  	face target goal 
	 *  	approach and kick ball 
	 * else set way-point 90 degrees or 270 degrees to the line connecting ball to goal (whichever is closer to the angle between line connecting ball to the target goal and the line connecting the ball to the Robot)
	 *		if way-point is within robots bounding box
	 * 			go to way-point face and move to the ball's approach point face target goal and
	 * 			approach and kick ball
	 * 		else
	 * 			Robot is stuck in a corner of its zone and can't kick the ball towards the goal.
	 * 			 display that shot has been deemed impossible
	 * else
	 * 	display that shot has been deemed impossible
	 * end
	 * 
	 * Throughout this loop if ball location is changed then loop should start
	 * from the top again after a period of waiting for the ball's reported
	 * position to stabilise
	 */

}
