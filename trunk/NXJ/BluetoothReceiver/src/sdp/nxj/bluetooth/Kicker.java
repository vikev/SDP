package sdp.nxj.bluetooth;

import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.addon.RCXMotor;

public class Kicker extends Thread {
	private double distance;

	public Kicker(double distance) {
		this.distance = distance;
	}

	public void run() {
		int d = (int) Math.round(distance);
		Motor.B.setSpeed(d);
		Motor.B.rotate(35);
		Motor.B.setSpeed(20);
		Motor.B.rotate(-35);
	}
}
