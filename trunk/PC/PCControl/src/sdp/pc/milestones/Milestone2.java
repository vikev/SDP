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
		new Vision();
		Thread.sleep(3000);
		Driver driver = new Driver(new TCPClient("localhost", 4456)); // Connect
		try {
			driver.stop();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Thread.sleep(500);
		int ballX, ballY, yellowX, yellowY;
		double yellowOrientation, targetAngle;
		double deltaX, deltaY;
		while (true) {
			ballX = state.getBallPosition().getX();
			ballY = state.getBallPosition().getY();
			yellowX = state.getRobotPosition(0, 0).getX();
			yellowY = state.getRobotPosition(0, 0).getY();
			yellowOrientation = state.getRobotFacing(0, 0);
			deltaX = yellowX - ballX;
			deltaY = ballY - yellowY;
			targetAngle = Math.atan(deltaY/deltaX) * 360 / (2*Math.PI);
			if (yellowOrientation - targetAngle < 20) {
				try {
					driver.stop();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			try {
				//if (yellowOrientation - targetAngle > 180) {
					driver.turnLeft(5);
				//} else {
				//	driver.turnRight(5);
				//}
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
