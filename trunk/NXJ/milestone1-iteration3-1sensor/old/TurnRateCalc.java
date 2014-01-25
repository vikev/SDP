import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;


public class TurnRateCalc {
	static DifferentialPilot pilot;
	
	static double currentRatio;
	static double minRatio = 0.1,
				maxRatio = 0.5;
	
	static double turnDegrees = 180;
	
	public static void main(String[] args) {
		int buttonPressed;
		do {
			newPilot();
			printRatio();
			buttonPressed = doTest();
			if(buttonPressed == Button.ID_LEFT)
				minRatio = currentRatio;
			if(buttonPressed == Button.ID_RIGHT)
				maxRatio = currentRatio;
		}
		while(buttonPressed != Button.ID_ENTER);
	}
	
	private static void printRatio() {
		LCD.clear(7);
		LCD.drawString("currentRatio " + (int)(currentRatio * 100), 0, 7);
	}
	
	private static void newPilot() {
		currentRatio = (maxRatio + minRatio) / 2;
		pilot = new DifferentialPilot(5 * currentRatio, 5, Motor.A, Motor.C);
		pilot.setRotateSpeed(Travel.ROTATE_SPEED);
	}
	
	private static int doTest() {
		LCD.clear(0);
		LCD.clear(1);
		LCD.clear(2);
		LCD.clear(3);
		LCD.drawString("We should turn ", 0, 0);
		LCD.drawString(turnDegrees + " deg", 0, 1);
		
		Button.waitForAnyPress();
		pilot.rotate(turnDegrees);
		
		LCD.clear(0);
		LCD.clear(1);
		LCD.clear(2);
		LCD.clear(3);
		LCD.drawString("Press:   Means:", 0, 0);
		LCD.drawString("Left      less", 0, 1);
		LCD.drawString("Right    more", 0, 2);
		LCD.drawString("Enter   exactly", 0, 3);
		
		return Button.waitForAnyPress();
	}
}
