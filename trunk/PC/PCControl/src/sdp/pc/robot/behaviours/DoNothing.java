package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

public class DoNothing extends RobotBehavior {

	public DoNothing(Robot robot) {
		super(robot);
	}

	@Override
	public boolean takeControl() {
		try {
			robot.getDriver().stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean actionFrame() throws Exception {
		return false;
	}

}
