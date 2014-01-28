package sdp.pc.milestones;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;

public class Milestone2 {
	
	private static WorldState state;
	
	public static void main (String[] args) throws V4L4JException, InterruptedException {
		state = new WorldState();
		new Vision(state);
		Thread.sleep(3000);
		Driver driver = new Driver(new TCPClient("localhost", 4456)); // Connect
		try {
			driver.stop();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Thread.sleep(500);
		int ballX;
		int ballY;
		int yellowX;
		int yellowY;
		double yellowOrientation = 25.0;
		double deltaX;
		double deltaY;
		double targetAngle = 0.0;
		while (true) {
			System.out.println(yellowOrientation - targetAngle);
			ballX = state.getBallX();
			ballY = state.getBallY();
			yellowX = state.getYellowX();
			yellowY = state.getYellowY();
			yellowOrientation = state.getYellowOrientation();
			deltaX = yellowX - ballX;
			deltaY = ballY - yellowY;
			targetAngle = Math.atan(deltaY/deltaX) * 360 / (2*Math.PI);
			if (yellowOrientation - targetAngle < 10) {
				try {
					driver.stop();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			try {
				driver.turnLeft(5);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Thread.sleep(100);
			/*targetAngle = Math.atan(deltaY/deltaX) * 360 / (2*Math.PI);
			try {
				driver.turnLeft(yellowOrientation - targetAngle);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		System.out.println("Congratulations!");
	}
}
