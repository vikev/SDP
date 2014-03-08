package sdp.pc.robot.behaviours;



import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Alg;
import sdp.pc.vision.Point2;
import sdp.pc.vision.relay.Driver;

public class GrabBall extends RobotBehavior {
	
	private static final double BALL_SPEED_THRESHOLD = 30;
	private static final double ANGLE_EPSILON = 10;
	private static final double DISTANCE_EPSILON = 50;
	
	public GrabBall(Robot robot) { super(robot); }

	/**
	 * Take control when the defending robot is not near the goal line
	 */
	@Override
	public boolean takeControl() {
		return robot.getWorldState().getBallSpeed() < BALL_SPEED_THRESHOLD 
				&& robot.getWorldState().getBallQuadrant() == myQuadrant();
	}
	
	/**
	 * Gets the quadrant we are supposed to be in
	 * TODO: check whether the robot is attacker/defender
	 */
	private int myQuadrant() {
		return robot.getWorldState().getAttackerQuadrant();
	}

	@Override
	public boolean actionFrame() throws Exception {
		Driver driver = robot.getDriver();
		
		Point2 ballPos = getWorldState().getBallPosition();
		Point2 myPos = robot.getPosition();
		double myFacing = robot.getFacing();
		double angleToBall = myPos.angleTo(ballPos);
		
		double ballRobotAngle = Alg.normalizeToBiDirection(angleToBall - myFacing);
		double ballRobotDist = myPos.distance(ballPos);
		System.out.printf("Dist: %.2f\t%.2f\t%.2f\t%.2f\t", myFacing, angleToBall, ballRobotDist, ballRobotAngle);
		if(Math.abs(ballRobotAngle) > ANGLE_EPSILON) {
			//turn
			System.out.println("turn");
			if(ballRobotAngle > 0)
				driver.turnRight(25);
			else
				driver.turnLeft(25);
		}
		else if(ballRobotDist > DISTANCE_EPSILON) {
			System.out.println("move");
			//move
			driver.forward(50);
		}
		else {
			System.out.println("grab");
			//grab!
			driver.stop();
			driver.grab();
			Thread.sleep(1000);
			return true;
		}
		
		return false;
	}	
}
