package sdp.nxt.interfaces.impl.regulatedchassisA;

import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import sdp.nxt.interfaces.Pilot;

public class DiffPilotA implements Pilot {
	private DifferentialPilot pilot;

	public DiffPilotA() {
		pilot = new DifferentialPilot(56, 135, Motor.A, Motor.C);
		// maximum power
		setPower(0);
	}

	private int power;

	public void forward(int power) {
		setPower(power);
		// pilot.travel(10000, true); // true means command is interrupted
		pilot.forward();
		// immediately by next command
	}

	/**
	 * Adjusts the pilot's rotation speed to degPerSec
	 * 
	 * @param degPerSec
	 *            rotate speed in degrees per second
	 */
	public void setPower(int degPerSec) {
		power = degPerSec;
		if (power == 0) {
			power = 90000;
		}
		pilot.setRotateSpeed(power);
		pilot.setTravelSpeed(power);
	}

	public void backward(int power) {
		setPower(power);
		// pilot.travel(10000, true); // true means command is interrupted
		pilot.backward();
	}

	public void turnLeft(int power) {
		setPower(power);
		// pilot.rotate(10000, true); // true means command is interrupted
		pilot.rotateLeft();
		// immediately by next command
	}

	public void turnRight(int power) {
		setPower(power);
		// pilot.rotate(10000, true); // true means command is interrupted
		pilot.rotateRight();
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
