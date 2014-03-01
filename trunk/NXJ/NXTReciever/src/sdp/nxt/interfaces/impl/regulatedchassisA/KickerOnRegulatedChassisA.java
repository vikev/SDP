package sdp.nxt.interfaces.impl.regulatedchassisA;

import lejos.nxt.Motor;
import sdp.nxt.interfaces.Kicker;

public class KickerOnRegulatedChassisA implements Kicker {

	private boolean closed;
	private int power;

	public KickerOnRegulatedChassisA(boolean isGrabberClosed) {
		this.closed = isGrabberClosed;
		if (closed) {
			kick(50);
		}
	}

	Thread mechThr = new Thread(new Runnable() {
		public void run() {
			if (power < 50) {
				power = 50;
			}
			// kick
			if (closed) {
				Motor.B.setSpeed(power);
				Motor.B.rotate(50);
				Motor.B.setSpeed(150);

				closed = false;
			} else { // grab
				Motor.B.setSpeed(150);
				Motor.B.rotate(-50);
				closed = true;
			}
		}
	});

	public void grab() {
		if (!closed && !mechThr.isAlive()) {
			mechThr.setDaemon(true);
			mechThr.run();
		}
	}

	public void kick(int power) {
		this.power = power;
		if (closed && !mechThr.isAlive()) {
			mechThr.setDaemon(true);
			mechThr.run();
		}
	}

	public void kick() {
		kick(50000);
	}

	public boolean isClosed() {
		return closed;
	}
}
