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
		kickStationaryBall(state, vision, driver);
		System.out.println("Finished attempting to kick ball");
	}

	// WIP Attempts to kick the ball by first navigating to a point
	// (apprachPoint) from which it can then approach the ball and kick it
	// towards the target goal
	// TODO:
	// * Navigate the robot around the ball when the ball obstructs the
	// direct path to the approach point.
	// * Look into how to behave if the ball is too close to the white boundary
	// to get behind it to take a shot at the goal.
	// * Create meaningful exception handling code,
	@SuppressWarnings("unused")
	public static void kickStationaryBall(WorldState state, Vision vision,
			Driver driver) {
		int diffX, diffY, cantakeShot, checkX;
		Point2 ballPosition = state.getBallPosition();
		System.out.println("ballAt " + ballPosition);

		// assume stored robot position refers to mean position of all pixels
		// in the mask
		Point2 robotPosition = state.getRobotPosition(0, 0);
		System.out.println("robot at" + robotPosition);
		Point2 goalCentre = new Point2(250, 250);
		System.out.println("goal centre at" + goalCentre);
		if (state.targetGoal == 1) {
			goalCentre = state.getLeftGoalCentre();
			checkX = robotPosition.getX() - 10;

			// check if the ball will obstruct a direct path to the approach
			// point move to a point where a direct path can be taken to the
			// approach point
			if (ballPosition.getX() >= (checkX)) {
			}
		} else {
			goalCentre = state.getLeftGoalCentre();
			checkX = robotPosition.getX() + 10;
			if (ballPosition.getX() <= (checkX)) {
				// move to a point where a direct path can be taken to the
				// approach point
			}
		}

		// Attempt to navigate to the approach point.
		if (1 == (cantakeShot = gotoapproachPoint(state, driver))) {
			facePoint(state, driver, goalCentre.getX(), goalCentre.getY());
			// get close to ball
			while (Math.abs(ballPosition.getX() - robotPosition.getX()) < 5) {
				try {
					driver.forward(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				driver.kick(40);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static int gotoapproachPoint(WorldState state, Driver driver) {
		int diffX, diffY, approachXpoint, approachYpoint;
		double gradientLineBalltoGoal;
		Point2 ballPosition = state.getBallPosition();

		// Initialise to an arbitrary Point
		Point2 targetGoalCentre = new Point2(250, 250);
		if (state.targetGoal == 1) {
			targetGoalCentre = state.getLeftGoalCentre();
		} else {
			targetGoalCentre = state.getRightGoalCentre();
		}

		diffX = ballPosition.getX() - targetGoalCentre.getX();
		diffY = ballPosition.getY() - targetGoalCentre.getY();
		if (diffX == 0) { // ball already in goal
			System.out.println("Ball already in target goal.");
			return 0;
		}

		// set approach point X coordinate to behind both the ball and the
		// target goal (number 10 is arbitrary)
		if (state.targetGoal == 1) {
			approachXpoint = ballPosition.getX() + 10;
		} else {
			approachXpoint = ballPosition.getX() - 10;
		}

		// calculate the approach point Y coordinate based on the gradient of a
		// straight line connecting the goal centre and the ball
		if (diffY == 0) {
			approachYpoint = ballPosition.getY();
		} else {
			gradientLineBalltoGoal = diffY / diffX;
			approachYpoint = (int) Math.floor(gradientLineBalltoGoal
					* (approachXpoint));
		}
		facePoint(state, driver, approachXpoint, approachYpoint);

		// move robot towards approach point with a tolerance of 5 pixels
		while ((Math.abs(ballPosition.getX() - approachXpoint) < 5)) {
			try {
				driver.forward(3);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return 1;
	}

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
		System.out.println("targetAngle");
		System.out.println(targetAngle);
		double rotateBy = robotOrientation - targetAngle;
		System.out.println("Rotate by:");
		System.out.println(rotateBy);
		while (Math.abs(rotateBy) > 20) {
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
	 * Call chooseRobot connect to chosen Robot create Vision instance discern
	 * target goal assign target point within goal (currently only use goal
	 * centre) - separate method create vectors for line ball to goal and for
	 * ball to robot using ball position as (0,0) point of coordinate axis
	 * assign ball's approach point if ball's approach point lies within
	 * selected robots bounding box then if angle between line connecting ball
	 * to the target goal and the line connecting the ball to the Robot is >90
	 * degrees and <270 move to the ball's approach point face target goal and
	 * approach and kick ball else set way-point 90 degrees or 270 degrees to
	 * the line connecting ball to goal (whichever is closer to the angle
	 * between line connecting ball to the target goal and the line connecting
	 * the ball to the Robot) if way-point is within robots bounding box go to
	 * way-point face and move to the ball's approach point face target goal and
	 * approach and kick ball else Robot is stuck in a corner of its bounding
	 * box and can't kick the ball towards the goal. display that shot has been
	 * deemed impossible else display that shot has been deemed impossible end
	 * 
	 * Throughout this loop if ball location is changed then loop should start
	 * from the top again after a period of waiting for the ball's reported
	 * position to stabilise
	 */

	@SuppressWarnings("unused")
	public static void kickball(WorldState state, TCPClient robotControl,
			Vision vision, int chosenRobot) {
		int targetGoal = state.getTargetGoal();
		if (chosenRobot == Constants.ATTACKER)
			chosenRobot = 1;
		if (chosenRobot == Constants.DEFENDER)
			chosenRobot = 0;
		Point2 targetPoint = setBallTarget(state);
		if (targetPoint.getX() == 0 & targetPoint.getY() == 0) {
			System.out
					.println("Could not assign ball target point within target goal");
			return; // could not set target point for ball and therefore cannot
					// take shot
		}
		int[] vectorLineBalltoTargetPoint = {
				(state.getBallPosition().getX() - targetPoint.getX()),
				(state.getBallPosition().getY() - targetPoint.getY()) };
		int[] vectorLineBalltoSelectedRobot = {
				(state.getBallPosition().getX() - state.getRobotPosition(0,
						chosenRobot).getX()),
				(state.getBallPosition().getY() - state.getRobotPosition(0,
						chosenRobot).getY()) };

	}

	public static Point2 setBallTarget(WorldState state) {
		if (1 == state.getTargetGoal()) // if target goal is the right hand goal
			return state.getRightGoalCentre();
		if (0 == state.getTargetGoal())
			return state.getLeftGoalCentre();
		return new Point2(0, 0);
	}
}
