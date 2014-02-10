package sdp.nxj.bluetooth;

import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;

public class DiffPilot {
	private DifferentialPilot pilot;

	public DiffPilot() {
		pilot = new DifferentialPilot(56, 135, Motor.A, Motor.C, true);
		setRotateSpeed(0);
	}

	public void driveForward(double distance) {
		if (distance == 0) { // interpret a distance of zero to mean
			pilot.forward(); // drive forward forever
		} else {
			pilot.travel(distance, true); // true means command is interrupted
		} // immediately by next command
	}

	/**
	 * Adjusts the pilot's rotation speed to degPerSec
	 * 
	 * @param degPerSec
	 *            rotate speed in degrees per second
	 */
	public void setRotateSpeed(double degPerSec) {
		if (degPerSec == 0) {
			degPerSec = 900000;
		}
		pilot.setRotateSpeed(degPerSec);
		pilot.setTravelSpeed(degPerSec);
	}

	public void driveBackward(double distance) {
		if (distance == 0) { // interpret a distance of zero to mean
			pilot.backward(); // drive backward forever
		} else {
			pilot.travel(-distance, true); // true means command is interrupted
		} // immediately by next command
	}

	public void turnLeft(double distance) {
		if (distance == 0) { // interpret a distance of zero to mean
			pilot.rotateLeft(); // rotate until given a stop command
		} else {
			pilot.rotate(distance, true); // true means command is interrupted
		} // immediately by next command
	}

	public void turnRight(double distance) {
		if (distance == 0) { // interpret a distance of zero to mean
			pilot.rotateRight(); // rotate until given a stop command
		} else {
			pilot.rotate(-distance, true); // true means command is interrupted
		} // immediately by next command
	}

	public void stopNow() {
		pilot.stop();
	}
}
