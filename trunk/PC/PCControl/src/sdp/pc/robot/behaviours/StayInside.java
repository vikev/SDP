package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

public class StayInside extends RobotBehavior {

	private static final int MOVE_BACK_DURATION = 500;

	public StayInside(Robot robot) {
		super(robot);
	}

	@Override
	public boolean takeControl() {		
		// TODO Auto-generated method stub
		startedMoving = false;
		return !robot.ownQuadrant();
	}

	private boolean startedMoving;
	
	@Override
	public boolean actionFrame() throws Exception {
		System.out.println("Stopped because of bounds");
		if(!startedMoving) {
			startedMoving = true;
			robot.getDriver().stop();
			robot.getDriver().backward(70);
			startTimeout();
		}
		
		if(waitTimeout(MOVE_BACK_DURATION))
			return false;
		
		robot.getDriver().stop();
		return true;
	}

}
