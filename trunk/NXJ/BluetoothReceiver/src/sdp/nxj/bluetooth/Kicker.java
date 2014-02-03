package sdp.nxj.bluetooth;

import lejos.nxt.Motor;

public class Kicker extends Thread {
	private double distance;

	public Kicker(double distance) {
		this.distance = distance;
	}

	public void run() {
		int d = (int) Math.round(distance);
		Motor.B.setSpeed(d);
		Motor.B.rotate(35);
		Motor.B.setSpeed(60);
		Motor.B.rotate(-35);
	}
}
