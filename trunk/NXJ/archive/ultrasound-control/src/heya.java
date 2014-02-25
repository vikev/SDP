import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;
 
public class heya {
//	static NXTMotor leftTire = new NXTMotor(MotorPort.A);
//	static NXTMotor rightTire = new NXTMotor(MotorPort.B);
//	
//	private static void stop() {
//		leftTire.stop();
//		rightTire.stop();
//	}
//	
//	private static void rotateLeft(int speed) {
//		leftTire.backward();
//		leftTire.setPower(speed);
//		rightTire.forward();
//		rightTire.setPower(speed);
//	}
//	
//	private static void rotateRight(int speed) {
//		leftTire.forward();
//		leftTire.setPower(speed);
//		rightTire.backward();
//		rightTire.setPower(speed);
//	}

	static UltrasonicControl s = new UltrasonicControl(SensorPort.S1);
	
	static DifferentialPilot pilot;
	
	static final int SLOW_DOWN_DIST = 15;
	
	
	public static void runToWall(double targetSpeed, int distToWall, boolean slowDown) {
		
		//print type
		LCD.clear(0);
		LCD.drawString("Moving to wall", 0, 0);
		
		//start moving
		pilot.setTravelSpeed(targetSpeed);
		pilot.forward();
		
		int d;
		double currentSpeed;
		
		
		
		while(true)
		{
			
			d = s.getFastMeasurement();
			currentSpeed = pilot.getTravelSpeed();
			
			LCD.clear(1);
			LCD.drawInt(d, 0, 1);
			LCD.drawString(Double.toString(targetSpeed), 5, 1);
			
			LCD.clear(2);
			if(d < distToWall)
			{
				pilot.stop();
				LCD.drawString("Stopped!", 0, 2);
				break;
			}
			else if(slowDown && d < distToWall + SLOW_DOWN_DIST && currentSpeed - 0.5 > targetSpeed / 2) {
				pilot.setTravelSpeed(targetSpeed / 2);
				LCD.drawString("Slow down!", 0, 2);
			}
			else if (currentSpeed < targetSpeed - 0.5){
				LCD.drawString("Move!", 0, 2);
				pilot.setTravelSpeed(targetSpeed);
			}
			
			safeSleep(100);
		}
		LCD.clear(1);
		LCD.drawString("done!", 0, 1);
	}

	/**
	 * A lazy sleep-exception-catcher
	 * @param ms The duration to sleep for
	 */
	private static void safeSleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LCD.drawString("ERROR", 0, 0);
		}
	}
	
	/**
	 * Tells us whether we should turn left (1), right (-1) or if it doesn't matter
	 * @return the direction which is best for us to turn to!
	 */
	public static int LeftOrRight() {
		int[] dist = s.getAngledDistances(pilot);
		//return highest dist
		if(dist[0] > dist[2])
			return -1;
		else if (dist[2] > dist[0])
			return 1;
		return 0;
		
	}
	
	/**
	 * Starts turning until the sensor says it's ok to stop
	 * @param howFarIsOk the distance the sensor should return before stopping
	 * @param direction the direction to spin. 1 for left; -1 for right
	 */
	public static void TurnUntilOk(int howFarIsOk, int speed, int direction) {
//		pilot.setRotateSpeed(rotateSpeed);
		LCD.clear(0);
		LCD.drawString("Turning " + (direction == 1 ? "left" : "right"), 0, 0);
		
		pilot.setRotateSpeed(speed);
		if(direction == 1)
			pilot.rotateLeft();
		else
			pilot.rotateRight();
		
		int dist = s.getFastMeasurement();
		
		while(dist < howFarIsOk) {
			LCD.clear(1);
			LCD.drawString(dist + " < " + howFarIsOk , 0, 1);
			safeSleep(50);
			dist = s.getFastMeasurement();
		}
		
		pilot.stop();
	}
	public static int[] rotateAndScan() {
		int[] retVal = new int[8];
		
		for(int i = 0; i < 8; i++) {
			pilot.rotate(45);
			retVal[i] = s.getFastMeasurement();
		}
		return retVal;
	}
	
	public static void main(String[] args) {
		//setup pilot 
		pilot = new DifferentialPilot(5.25f, 12f,(NXTRegulatedMotor) Motor.A,(NXTRegulatedMotor) Motor.C, true);
		
		while(true) {
			double sug = s.getSuggestedRotateAngle(pilot, 15);
			LCD.clear(3);
			LCD.drawInt((int)sug, 0, 3);
			Button.waitForAnyPress();
			pilot.rotate(sug);
			Button.waitForAnyPress();
		}
		
////		int[] sc = rotateAndScan();
////		LCD.drawString(sc[0] + " " + sc[1] + " " + sc[2] + " " + sc[3], 0, 0);
////		LCD.drawString(sc[4] + " " + sc[5] + " " + sc[6] + " " + sc[7], 0, 1);
////		LCD.drawString((int)pilot.getRotateSpeed() + " " + (int)pilot.getMaxRotateSpeed(), 0, 0);
//		Button.waitForAnyPress();
//		
//		while(true) {
//			runToWall(10, 1, true);
////			safeSleep(1000);
//			int leftOrRight = LeftOrRight();
//			if(leftOrRight == 0)
//				leftOrRight = 1;
//			leftOrRight = -leftOrRight;
////			safeSleep(1000);
//			TurnUntilOk(20, 70, leftOrRight);
//		}
		
//		Button.waitForAnyPress();
	}
}
 