package sdp.nxt.interfaces.impl.regulatedchassis;

import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import sdp.nxt.interfaces.Pilot;

public class DiffPilot implements Pilot {
	private DifferentialPilot pilot;

	public DiffPilot() {
		pilot = new DifferentialPilot(56, 135, Motor.A, Motor.C, true);
		setPower(0);
	}

	private int power;

	public void forward(int power) {
		setPower(power);
		pilot.travel(power, true); // true means command is interrupted
		// immediately by next command
	}

	/**
	 * Adjusts the pilot's rotation speed to degPerSec
	 * 
	 * @param degPerSec
	 *            rotate speed in degrees per second
	 */
	public void setPower(int degPerSec) {
		if (degPerSec == 0) {
			degPerSec = 900000;
		}
		power = degPerSec;
		pilot.setRotateSpeed(degPerSec);
		pilot.setTravelSpeed(degPerSec);
	}

	public void backward(int power) {
		setPower(power);
		pilot.travel(-power, true); // true means command is interrupted
	}

	public void turnLeft(int power) {
		setPower(power);
		pilot.rotate(power, true); // true means command is interrupted
		// immediately by next command
	}

	public void turnRight(int power) {
		setPower(power);
		pilot.rotate(-power, true); // true means command is interrupted
		// immediately by next command
	}

	public void stop() {
		pilot.stop();
	}

	public int getPower() {
		return power;
	}

	public void forward() {
		forward(90000);
	}

	public void backward() {
		backward(90000);
	}

}
