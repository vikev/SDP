package sdp.pc.milestones;


import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;
import sdp.pc.common.*;

public class Milestone3def {

	private static WorldState state = new WorldState();

	public static void main(String[] args) throws Exception {
		Vision vision = new Vision(state);
		Thread.sleep(3000);

		Driver driver = new Driver(new TCPClient(ChooseRobot.dialog()));

		try {
			driver.stop();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Thread.sleep(500);
		defenceBall(state, vision, driver);
		System.out.println("Finished attempting to defend the ball");
	}

	public static void defenceBall(WorldState state, Vision vision,
			Driver driver) {
		Point2 ballPosition = state.getBallPosition();
		System.out.println("Ball is at: " + ballPosition);
		
		//At the beginning make sure that robot is facing by 270 degrees
		rotateToRight(state, vision, driver);
		
	}
	
	public static void rotateToRight(WorldState state, Vision vision,
			Driver driver) {
		
		Point2 robotPosition = state.getRobotPosition(0, 0);
		System.out.println("Initially robot is at: " + robotPosition);
		
		double robotFacing = state.getRobotFacing(0, 0);
		System.out.println("Initial robot facing angle: " + robotFacing);
		
		if (robotFacing > 90 & robotFacing < 270) {
			try {
				driver.turnRight(270 - robotFacing);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				driver.turnLeft(270 - robotFacing);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		double shit = 270 - robotFacing;
		System.out.println("Turned by: " + shit);
	}
	
}
