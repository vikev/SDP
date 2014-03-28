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

	private byte speed = (byte) 200;
	private byte supportSpeed = (byte) 100;

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
		kick = false;
	}

	public void kick(int power) {
		speedKick = power;
		kick = true;
	}

	public void kick() {
		kick(50000);
	}

	public boolean isClosed() {
		return closed;
	}

	/**
	 * what should i do grab/kick true = kick false = grab
	 */
	private boolean kick = true;

	/**
	 * what I have done true = grabbed false = opened/kicked
	 */
	private boolean closed = false;

	/**
	 * Flag for the open command
	 */
	private boolean open = false;

	private int speedKick = 9999999;
	Thread kickingThread = new Thread(new Runnable() {
		public void run() {
			int i = 0;
			while (true) {
				try {
					// kick or open
					if ((kick || open) && closed) {
						System.out.println(open);
						I2Csensor.sendData(0x02, speed);
						I2Csensor.sendData(0x01, backward);
						Thread.sleep(150);
						// just open without kicking
						if (!open) {
							Motor.B.setSpeed(speedKick);
							Motor.B.rotate(-80);
							Motor.B.rotate(80);
						} else {
							kick = true;
						}
						I2Csensor.sendData(0x01, off);
						closed = false;
						open = false;
					} else // grab
					if (!kick && !closed) {
						I2Csensor.sendData(0x02, speed);
						I2Csensor.sendData(0x01, forward);
						Thread.sleep(800);
						I2Csensor.sendData(0x01, off);
						closed = true;
						open = false;
					} else if (i > 7) {
						i = 0;
						I2Csensor.sendData(0x02, supportSpeed);
						if (closed) // keep open
						{
							I2Csensor.sendData(0x01, forward);
						} else { // keep closed
							I2Csensor.sendData(0x01, backward);
						}
					}
					Thread.sleep(100);
					I2Csensor.sendData(0x01, off);
					i++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	});

	/**
	 * Just open without kicking
	 */
	public void open() {
		open = true;
	}
}
