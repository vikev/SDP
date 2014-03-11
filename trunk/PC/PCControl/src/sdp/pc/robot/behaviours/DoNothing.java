package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

public class DoNothing extends RobotBehavior {

	public DoNothing(Robot robot) {
		super(robot);
	}

	private boolean takenControl;
	
	@Override
	public boolean takeControl() {
		takenControl = false;
		return true;
	}

	@Override
	public boolean actionFrame() throws Exception {
		if(!takenControl) {
			takenControl = true;
			robot.getDriver().stop();
		}
		return false;
	}

}
