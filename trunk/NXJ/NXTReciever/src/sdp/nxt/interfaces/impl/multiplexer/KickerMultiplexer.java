package sdp.nxt.interfaces.impl.multiplexer;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import sdp.nxt.interfaces.Kicker;

public class KickerMultiplexer implements Kicker {

	private boolean closed;
	private int power;

	private I2CSensor I2Csensor;

	private byte off = (byte) 0;
	private byte forward = (byte) 1;
	private byte backward = (byte) 2;
	private byte applyBreak = (byte) 3;

	private byte speed = (byte) 100;

	public KickerMultiplexer(boolean isGrabberClosed) {
		Motor.B.rotate(-1);
		I2CPort I2Cport;
		// Create a I2C port
		I2Cport = SensorPort.S4;
		// Assign port
		I2Cport.i2cEnable(I2CPort.STANDARD_MODE);
		// Initialize port in standard mode
		I2Csensor = new I2CSensor(I2Cport);
		I2Csensor.setAddress(0xB4);
		I2Csensor.sendData(0x02, speed);
		I2Csensor.sendData(0x01, backward);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		I2Csensor.sendData(0x01, off);
		kickingThread.setDaemon(false);
		kickingThread.start();
	}

	public void grab() {
		action = false;
	}

	public void kick(int power) {
		speedKick = power;
		action = true;
	}

	public void kick() {
		kick(50000);
	}

	public boolean isClosed() {
		return status;
	}

	/**
	 * what should i do grab/kick true = kick false = grab
	 */
	private boolean action = true;

	/**
	 * what I have done true = grabbed false = opened/kicked
	 */
	private boolean status = false;

	private int speedKick = 9999999;
	Thread kickingThread = new Thread(new Runnable() {
		public void run() {
			while (true) {
				try {
					// kick
					if (action && status) {
						I2Csensor.sendData(0x01, backward);
						Thread.sleep(300);
						Motor.B.setSpeed(speedKick);
						Motor.B.rotate(80);
						Motor.B.rotate(-80);
						I2Csensor.sendData(0x01, off);
						status = !status;
						// grab
					} else if (!action && !status) {
						I2Csensor.sendData(0x01, forward);
						Thread.sleep(400);
						I2Csensor.sendData(0x01, off);
						status = !status;
					}
					Thread.sleep(200);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	});
}
