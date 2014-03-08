package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Point2;
import sdp.pc.vision.relay.Driver;

public class KickToGoal extends RobotBehavior {
	private static final double ANGLE_EPSILON = 6;

	public KickToGoal(Robot robot) {
		super(robot);
	}

	@Override
	public boolean takeControl() {
		return robot.hasBall();
	}

	@Override
	public boolean actionFrame() throws Exception {
		Point2 goalPos = getWorldState().getOpponentGoalCenter();
		Point2 myPos = robot.getPosition();
		double ang = myPos.angleTo(goalPos);
		Driver driver = robot.getDriver();
		
		if(Math.abs(ang) > ANGLE_EPSILON) {
			if(ang < 0)
				driver.turnLeft(25);
			else
				driver.turnRight(25);
		}
		else
		{
			driver.stop();
			driver.kick(100);
		}
		return false;
	}

	
}
