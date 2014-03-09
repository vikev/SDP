package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

public class StayInside extends RobotBehavior {

	public StayInside(Robot robot) {
		super(robot);
	}

	@Override
	public boolean takeControl() {		
		// TODO Auto-generated method stub
		return !robot.ownQuadrant();
	}

	@Override
	public boolean actionFrame() throws Exception {
		System.out.println("Stopped because of bounds");
		robot.getDriver().stop();
		return true;
	}

}
