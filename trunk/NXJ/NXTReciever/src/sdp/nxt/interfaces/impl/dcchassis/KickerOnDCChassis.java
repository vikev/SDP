package sdp.nxt.interfaces.impl.dcchassis;

import lejos.nxt.Motor;
import sdp.nxt.interfaces.Kicker;

public class KickerOnDCChassis implements Kicker {
	private boolean closed;

	public KickerOnDCChassis(boolean isGrabberClosed) {
		this.closed = isGrabberClosed;
		if (closed) {
			kick(50);
		} else {
			Motor.B.setSpeed(50);
			Motor.B.rotate(15);
		}
	}

	Thread mechThr = new Thread(new Runnable() {
		public void run() {
			if (power < 50) {
				power = 50;
			}
			if (closed) {
				Motor.B.setSpeed(power);
				Motor.B.rotate(-50);
				Motor.B.setSpeed(50);
				Motor.B.rotate(15);
				closed = false;
			} else {
				Motor.B.setSpeed(150);
				Motor.B.rotate(35);
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

	int power;

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
