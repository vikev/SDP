package sdp.nxj.bluetooth;

import lejos.nxt.Motor;

public class Kicker {
	public static void kick(double angle){
		Motor.B.setSpeed(900);
		int rot = (int) Math.round(angle);
		Motor.B.rotate(rot);
		Motor.B.rotate(-rot);
	}
}
