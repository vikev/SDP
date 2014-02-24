package sdp.pc.robot.pilot;

import sdp.pc.common.ChooseRobot;
import sdp.pc.common.Constants;
import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;

public class GrabberTest {
	
	private final static double SAFE_ANGLE = 5;
	private final static int SAFE_DIST = 30;
	private final static int GRAB_DIST = 40;
	
	private static WorldState state = new WorldState();

	public static void main(String[] args) throws Exception {

		new Vision(state);
		Thread.sleep(3000);
		final TCPClient conn = new TCPClient(ChooseRobot.dialog());
		final Driver driver = new Driver(conn);

		try {
			driver.stop();
		} catch (Exception e1) {
			e1.printStackTrace();  
		}
		Thread.sleep(500);

		// Shutdown hook so one does not have to re-run 'ant' every time
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Exiting and stopping the robot");
				try {
					driver.stop();
					conn.closeConnection();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Should have stopped by now.");
			}
		});
		Thread.sleep(500);
		try {
			driver.kick(1000);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		int team = state.getOurColor();
		int dir = state.getDirection();
		
		Robot robot = new Robot(driver, state, team, dir);
		System.out.println(state.getRobotPosition(team, dir));
		
		while (true) {
			Point2 ballPos = state.getBallPosition();
			Point2 robotPos = state.getRobotPosition(team, dir);

			if (robot.goTo(ballPos, SAFE_DIST)) {
				driver.stop();
				Thread.sleep(50);	
				if (ballPos.distance(robotPos) < GRAB_DIST) {
					driver.grab(60);
					Thread.sleep(200);
					if (ballPos.distance(robotPos) < GRAB_DIST) {
						Point2 target = new Point2();
						if (dir == Constants.DIRECTION_LEFT) {
							target = state.getLeftGoalCentre();
						}
						else {
							target = state.getRightGoalCentre();
						}
						while (!robot.turnTo(target, SAFE_ANGLE)) {
							Thread.sleep(100);
						}
						driver.stop();
						driver.kick(2000);
					}
					else {
						driver.kick(2000);
					}
				}
			}
			Thread.sleep(100);
		}
	}
	
}
