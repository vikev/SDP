package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

public class KickToGoal extends RobotBehavior {

	public KickToGoal(Robot robot) {
		super(robot);
	}

	@Override
	public boolean takeControl() {
		return robot.hasBall();
	}

	@Override
	public boolean actionFrame() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	
}
