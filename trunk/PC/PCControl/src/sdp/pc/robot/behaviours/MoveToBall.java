package sdp.pc.robot.behaviours;



import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Alg;
import sdp.pc.vision.Point2;
import sdp.pc.vision.relay.Driver;

public class MoveToBall extends RobotBehavior {
	
	private static final double BALL_SPEED_THRESHOLD = 30;
	private static final double ANGLE_EPSILON = Robot.ANGLE_EPSILON;
	private static final double DISTANCE_EPSILON = Robot.DIST_EPSILON;
	
	public MoveToBall(Robot robot) { super(robot); }

	/**
	 * Take control when all of the following are true:
	 * 	- ball is not moving with sufficient velocity
	 *  - ball is in our quadrant
	 */
	@Override
	public boolean takeControl() {
		
		return robot.getWorldState().getBallSpeed() < BALL_SPEED_THRESHOLD 
				&& robot.getWorldState().getBallQuadrant() == myQuadrant()
				&& !robot.iHaveBall();
	}
	
	/**
	 * Gets the quadrant we are supposed to be in
	 */
	private int myQuadrant() {
		return robot.getMyQuadrant();
	}

	@Override
	public boolean actionFrame() throws Exception {
		Driver driver = robot.getDriver();
		
		// stop the state if the ball is out of our pitch
		if(robot.getWorldState().getBallQuadrant() != myQuadrant())
			return true;
		
		double myFacing = robot.getFacing();
		double angleToBall = robot.getAngleToBall();
		
		double ballRobotAngle = Alg.normalizeToBiDirection(angleToBall - myFacing);
		double ballRobotDist = robot.getDistanceToBall();
		
		System.out.printf("Dist: %.2f\t%.2f\t%.2f\t%.2f\t", myFacing, angleToBall, ballRobotDist, ballRobotAngle);
		if(Math.abs(ballRobotAngle) > ANGLE_EPSILON) {
			//turn
			System.out.println("turn");
			int rSpeed = Robot.getRotateSpeed(ballRobotAngle, ANGLE_EPSILON)/3;
			if(ballRobotAngle > 0)
				driver.turnRight(rSpeed);
			else
				driver.turnLeft(rSpeed);
		}
		else if(ballRobotDist > DISTANCE_EPSILON) {
			System.out.println("move");
			int mSpeed = robot.getMoveSpeed(ballRobotDist)/3;
			driver.forward(mSpeed);
		}
		else {
			System.out.println("done");
			//driver.stop();
			return true;
		}
		
		return false;
	}	
}
