package sdp.pc.milestones;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;

import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;
import sdp.pc.common.*;

/**
 * This class should be reimplemented to use ChooseRobot and TCPClient like code
 * insted of Driver.
 * 
 * @author s1117764
 * 
 */
public class Milestone2 {

	private static WorldState state = new WorldState();

	public static void main(String[] args) throws V4L4JException, InterruptedException {
		Vision vision = new Vision(state);
		Thread.sleep(3000);

		Driver driver = new Driver(new TCPClient(Constants.HOST,
				Constants.ATTACKER_PORT));
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
	public static void kickStationaryBall(WorldState state, Vision vision, Driver driver) {
		int diffX, diffY, cantakeShot, checkX;
		Point2 ballPosition = state.getBallPosition();
		System.out.println(ballPosition);

		// assume stored robot position refers to mean position of all pixels
		// in the mask
		Point2 robotPosition = state.getRobotPosition(0, 0);
		Point2 goalCentre = new Point2(250, 250);
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
		int deltaY, deltaX;
		Point2 robotPosition = state.getRobotPosition(0, 0);
		Point2 ballPosition = state.getBallPosition();
		double robotOrientation = state.getRobotFacing(0, 0);
		deltaX = robotPosition.getX() - ballPosition.getX();
		deltaY = ballPosition.getY() - robotPosition.getY();
		double targetAngle = Math.atan(deltaY / deltaX) * 360 / (2 * Math.PI);
		System.out.println(targetAngle);
		double rotateBy = robotOrientation - targetAngle;
		while (Math.abs(rotateBy) > 20) {
			try {
				if (rotateBy > 0) {
					if (rotateBy > 180) {
						driver.turnLeft(5);
					} else {
						driver.turnRight(5);
					}
				} else {
					if (-rotateBy > 180) {
						driver.turnRight(5);
					} else {
						driver.turnLeft(5);
					}
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
			robotOrientation = state.getRobotFacing(0, 0);
			rotateBy = robotOrientation - targetAngle;
		}
	}
	// private static WorldState state = new WorldState();
	//
	// public static void main(String[] args) throws V4L4JException,
	// InterruptedException {
	// new Vision(state);
	// Thread.sleep(3000);
	//
	// Driver driver = new Driver(new TCPClient(Constants.HOST,
	// Constants.CLIENT_PORT));
	// try {
	// driver.stop();
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// }
	// Thread.sleep(500);
	// kickStationaryBall(state, driver);
	// System.out.println("Finished attempting to kick ball");
	// }
	//
	// // WIP Attempts to kick the ball by first navigating to a point
	// // (apprachPoint) from which it can then approach the ball and kick it
	// // towards the target goal
	// // TODO:
	// // * Navigate the robot around the ball when the ball obstructs the
	// // direct path to the approach point.
	// // * Look into how to behave if the ball is too close to the white
	// boundary
	// // to get behind it to take a shot at the goal.
	// // * Create meaningful exception handling code,
	// @SuppressWarnings("unused")
	// public static void kickStationaryBall(WorldState state, Driver driver) {
	// int diffX, diffY, cantakeShot, checkX;
	// Point2 ballPosition = state.getBallPosition();
	//
	// // assume stored robot position refers to mean position of all pixels
	// // in the mask
	// Point2 robotPosition = state.getRobotPosition(0, 0);
	// Point2 goalCentre = new Point2(250, 250);
	// if (state.targetGoal == 1) {
	// goalCentre = state.getLeftGoalCentre();
	// checkX = robotPosition.getX() - 10;
	//
	// // check if the ball will obstruct a direct path to the approach
	// // point move to a point where a direct path can be taken to the
	// // approach point
	// if (ballPosition.getX() >= (checkX)) {
	// }
	// } else {
	// goalCentre = state.getLeftGoalCentre();
	// checkX = robotPosition.getX() + 10;
	// if (ballPosition.getX() <= (checkX)) {
	// // move to a point where a direct path can be taken to the
	// // approach point
	// }
	// }
	//
	// // Attempt to navigate to the approach point.
	// if (1 == (cantakeShot = gotoapproachPoint(state, driver))) {
	// facePoint(state, driver, goalCentre.getX(), goalCentre.getY());
	// // get close to ball
	// while (Math.abs(ballPosition.getX() - robotPosition.getX()) < 5) {
	// try {
	// driver.forward(1);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// try {
	// Thread.sleep(50);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// try {
	// driver.kick(40);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// public static int gotoapproachPoint(WorldState state, Driver driver) {
	// int diffX, diffY, approachXpoint, approachYpoint;
	// double gradientLineBalltoGoal;
	// Point2 ballPosition = state.getBallPosition();
	//
	// // Initialise to an arbitrary Point
	// Point2 targetGoalCentre = new Point2(250, 250);
	// if (state.targetGoal == 1) {
	// targetGoalCentre = state.getLeftGoalCentre();
	// } else {
	// targetGoalCentre = state.getRightGoalCentre();
	// }
	//
	// diffX = ballPosition.getX() - targetGoalCentre.getX();
	// diffY = ballPosition.getY() - targetGoalCentre.getY();
	// if (diffX == 0) { // ball already in goal
	// System.out.println("Ball already in target goal.");
	// return 0;
	// }
	//
	// // set approach point X coordinate to behind both the ball and the
	// // target goal (number 10 is arbitrary)
	// if (state.targetGoal == 1) {
	// approachXpoint = ballPosition.getX() + 10;
	// } else {
	// approachXpoint = ballPosition.getX() - 10;
	// }
	//
	// // calculate the approach point Y coordinate based on the gradient of a
	// // straight line connecting the goal centre and the ball
	// if (diffY == 0) {
	// approachYpoint = ballPosition.getY();
	// } else {
	// gradientLineBalltoGoal = diffY / diffX;
	// approachYpoint = (int) Math.floor(gradientLineBalltoGoal
	// * (approachXpoint));
	// }
	// facePoint(state, driver, approachXpoint, approachYpoint);
	//
	// // move robot towards approach point with a tolerance of 5 pixels
	// while ((Math.abs(ballPosition.getX() - approachXpoint) < 5)) {
	// try {
	// driver.forward(3);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// try {
	// Thread.sleep(100);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// return 1;
	// }
	//
	// public static void facePoint(WorldState state, Driver driver,
	// int Xcoordinate, int Ycoordinate) {
	// int deltaY, deltaX;
	// Point2 robotPosition = state.getRobotPosition(0, 0);
	// Point2 ballPosition = state.getBallPosition();
	// double robotOrientation = state.getRobotFacing(0, 0);
	// deltaX = robotPosition.getX() - ballPosition.getX();
	// deltaY = ballPosition.getY() - robotPosition.getY();
	// double targetAngle = Math.atan(deltaY / deltaX) * 360 / (2 * Math.PI);
	// System.out.println(targetAngle);
	// double rotateBy = robotOrientation - targetAngle;
	// while (Math.abs(rotateBy) > 20) {
	// try {
	// if (rotateBy > 0) {
	// if (rotateBy > 180) {
	// driver.turnLeft(5);
	// } else {
	// driver.turnRight(5);
	// }
	// } else {
	// if (-rotateBy > 180) {
	// driver.turnRight(5);
	// } else {
	// driver.turnLeft(5);
	// }
	// }
	// } catch (Exception e1) {
	// System.out
	// .println("Exception encountered while trying to turn to face point "
	// + Xcoordinate + ", " + Ycoordinate);
	// e1.printStackTrace();
	// }
	// try {
	// Thread.sleep(100);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// robotOrientation = state.getRobotFacing(0, 0);
	// rotateBy = robotOrientation - targetAngle;
	// }
	// }
}
