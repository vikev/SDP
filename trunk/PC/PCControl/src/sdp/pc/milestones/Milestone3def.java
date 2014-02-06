package sdp.pc.milestones;


import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;
import sdp.pc.common.*;
import java.util.Scanner;

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
		while (true) {
			Scanner reader = new Scanner(System.in);
			System.out.println("Enter the first number");
			//get user input for a
			int a = reader.nextInt();
			if (a == 0) {
				defendBall(state, vision, driver);
				System.out.println("Finished attempting to defend the ball");
			}
		}
	}

	public static void defendBall(WorldState state, Vision vision,
			Driver driver) {
		Point2 ballPosition = state.getBallPosition();
		System.out.println("Ball is at: " + ballPosition);
		
		//At the beginning make sure that robot is facing perpendicular to edges
		rotatePerpendicular(state, vision, driver);
	}
	
	public static void rotatePerpendicular(WorldState state, Vision vision,
			Driver driver) {
		
		Point2 robotPosition = state.getRobotPosition(0, 0);
		System.out.println("Initially robot is at: " + robotPosition);
		
		double robotFacing = state.getRobotFacing(0, 0);
		System.out.println("Initial robot facing angle: " + robotFacing);
		
		double rotateBy;
		boolean rotateDown = false;
		
		if (robotFacing > 0 & robotFacing < 180) {
			rotateBy = robotFacing - 90;
			rotateDown = true;
		} else {
			rotateBy = robotFacing - 270;
		}
		
		while (Math.abs(rotateBy) > 10) {
			if (rotateDown) {
				if (robotFacing < 90) {
					try {
						driver.turnRight(5);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						driver.turnLeft(5);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				if (robotFacing < 270) {
					try {
						driver.turnRight(5);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						driver.turnLeft(5);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			robotFacing = state.getRobotFacing(0, 0);
			
			if (robotFacing > 0 & robotFacing < 180) {
				rotateBy = robotFacing - 90;
			} else {
				rotateBy = robotFacing - 270;
			} 
		}
		double shit = 270 - robotFacing;
		System.out.println("Turned by: " + shit);
	}
}
