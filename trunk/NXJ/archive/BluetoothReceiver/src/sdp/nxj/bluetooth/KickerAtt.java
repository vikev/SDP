package sdp.nxj.bluetooth;

import lejos.nxt.Motor;

public class KickerAtt extends Thread {
	private double power;

	public KickerAtt(double power) {
		this.power = power;
	}

	public void run() {
		int d = (int) Math.round(power);
		Motor.B.setSpeed(d);
		Motor.B.rotate(45);
		Motor.B.setSpeed(60);
		Motor.B.rotate(-45);
	}
}
