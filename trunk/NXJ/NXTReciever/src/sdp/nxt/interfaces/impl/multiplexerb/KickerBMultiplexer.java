package sdp.nxt.interfaces.impl.multiplexerb;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import sdp.nxt.interfaces.Kicker;

public class KickerBMultiplexer implements Kicker {
	private I2CSensor I2Csensor;

	private byte off = (byte) 0;
	private byte forward = (byte) 1;
	private byte backward = (byte) 2;

	private byte speed = (byte) 100;
	private byte supportSpeed = (byte) 50;

	@SuppressWarnings("deprecation")
	public KickerBMultiplexer(boolean isGrabberClosed) {
		Motor.B.rotate(-1);
		I2CPort I2Cport;
		// Create a I2C port
		I2Cport = SensorPort.S4;
		// Assign port
		I2Cport.i2cEnable(I2CPort.STANDARD_MODE);

		// Initialise port in standard mode
		I2Csensor = new I2CSensor(I2Cport);

		// TODO:
		I2Csensor.setAddress(0xB4);
		I2Csensor.sendData(0x02, speed);
		I2Csensor.sendData(0x01, backward);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
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
			int i = 0;
			while (true) {
				try {
					// kick
					if (action && status) {
						I2Csensor.sendData(0x02, speed);
						I2Csensor.sendData(0x01, backward);
						Thread.sleep(300);
						Motor.B.setSpeed(speedKick);
						Motor.B.rotate(-80);
						Motor.B.rotate(80);
						I2Csensor.sendData(0x01, off);
						status = !status;
						// grab
					} else if (!action && !status) {
						I2Csensor.sendData(0x02, speed);
						I2Csensor.sendData(0x01, forward);
						Thread.sleep(800);
						I2Csensor.sendData(0x01, off);
						status = !status;
					} else if (i > 7) {
						i = 0;
						I2Csensor.sendData(0x02, supportSpeed);
						if (status) // keep open
						{
							I2Csensor.sendData(0x01, forward);
						} else { // keep closed
							I2Csensor.sendData(0x01, backward);
						}
					}
					Thread.sleep(200);
					I2Csensor.sendData(0x01, off);
					i++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	});
}
