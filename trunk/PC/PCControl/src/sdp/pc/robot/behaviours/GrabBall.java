package sdp.pc.robot.behaviours;



import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Point2;
import sdp.pc.vision.relay.Driver;

public class GrabBall extends RobotBehavior {
	
	private static final double BALL_SPEED_THRESHOLD = 30;
	private static final double ANGLE_EPSILON = 6;
	private static final double DISTANCE_EPSILON = 10;
	
	public GrabBall(Robot robot) { super(robot); }

	/**
	 * Take control when the defending robot is not near the goal line
	 */
	@Override
	public boolean takeControl() {
		return robot.getWorldState().getBallSpeed() < BALL_SPEED_THRESHOLD 
				&& robot.getWorldState().getBallQuadrant() == myQuadrant()
				&& !robot.hasBall();
	}
	
	/**
	 * Gets the quadrant we are supposed to be in
	 * TODO: check whether the robot is attacker/defender
	 */
	private int myQuadrant() {
		return robot.getWorldState().getDefenderQuadrant();
	}

	@Override
	public void action() {
		boolean turning = false;
		Driver driver = robot.getDriver();
		
		while (!suppressed && takeControl()) {
			try {
				
				Point2 ballPos = getWorldState().getBallPosition();
				Point2 myPos = robot.getPosition();
				
				double ballRobotAngle = myPos.angleTo(ballPos);
				double ballRobotDist = myPos.distance(ballPos);
				
				if(ballRobotAngle > ANGLE_EPSILON) {
					if(ballRobotAngle > 0)
						driver.turnLeft();
					else
						driver.turnRight();
				}
				else if(ballRobotDist > DISTANCE_EPSILON) {
					//start moving

					driver.forward();
				}
				else if(!robot.hasBall()) {
					//grab!
					robot.getDriver().grab();
				}
				else
					break;
				
				sleep();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
}
