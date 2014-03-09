package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Alg;
import sdp.pc.vision.Point2;
import sdp.pc.vision.relay.Driver;

public class KickToGoal extends RobotBehavior {
	private static final double ANGLE_EPSILON = 3;
	private static final int GRAB_TIMEOUT = 1000;
	private static final double BALL_EPSILON_SPEED = 5;
	
	boolean ballGrabbed = false;

	public KickToGoal(Robot robot) {
		super(robot);
	}

	/**
	 * Take control only when robot has a ball
	 */
	@Override
	public boolean takeControl() {
		boolean hasBall = robot.iHaveBall();
		if(!hasBall && ballGrabbed) {
			ballGrabbed = false;
			System.out.println("lost grab!");
		}
		return hasBall && getWorldState().getBallSpeed() < BALL_EPSILON_SPEED;
	}

	@Override
	public boolean actionFrame() throws Exception {
		Driver driver = robot.getDriver();
		
		if(!ballGrabbed) {
			driver.stop();
			driver.grab();
			startTimeout();
			ballGrabbed = true;
			System.out.println("grab!");
			return false;
		}
		
		if (waitTimeout(GRAB_TIMEOUT)) {
			System.out.println("grabbing!");
			return false;
		}

		double myFacing = robot.getFacing();
		double goalAngle = robot.getAngleToOppositeGoal();

		double goalRobotAngle = Alg.normalizeToBiDirection(goalAngle - myFacing);
		
		if(Math.abs(goalRobotAngle) > ANGLE_EPSILON) {
			System.out.printf("turning! %.2f - %.2f = %.2f\n", goalAngle, myFacing, goalRobotAngle);
			int rSpeed = Robot.getRotateSpeed(goalRobotAngle, ANGLE_EPSILON)/3;
			if(goalRobotAngle < 0)
				driver.turnLeft(rSpeed);
			else
				driver.turnRight(rSpeed);
			return false;
		}
		else
		{
			System.out.println("kick!");
			driver.stop();
			driver.kick(5000);
			return true;
		}
	}

	
}
