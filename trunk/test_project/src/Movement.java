import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.nxt.TouchSensor;
import lejos.nxt.SensorPort;

public class Movement {
	DifferentialPilot pilot;
	TouchSensor stop;

	public Movement() {
		pilot = new DifferentialPilot(2.25f, 5.5f, Motor.A, Motor.C, true);
	}

	public void turn() {

	}

	public void driveForward() {
		while (!stop.isPressed()) {
			System.out.println("Forward");
			pilot.setRotateSpeed(0.5);
			pilot.forward();
			while (ColorScanner.isOnWhite()) {
				pilot.stop();
			}
			switch(ColorScanner.getNewDirection()){
				case BACKWARD: pilot.rotate(180); System.out.println("Backward");break;
				case LEFT: pilot.rotate(90); System.out.println("Left"); break;
				case RIGHT: pilot.rotate(-90); System.out.println("Right");break;
			}
	}

	public static void main(String args[]) {
		Movement move = new Movement();
		move.driveForward();
	}

}
