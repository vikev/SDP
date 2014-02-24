package sdp.nxt.interfaces.impl.dcchassis;

import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import sdp.nxt.interfaces.Pilot;

public class DCPilot implements Pilot {
	private NXTMotor m1 = new NXTMotor(MotorPort.A);
	private NXTMotor m2 = new NXTMotor(MotorPort.C);

	private int power;

	public DCPilot() {
		setPower(0);
	}

	/**
	 * The power is percentage. 0-100
	 */
	public void forward(int power) {
		this.setPower(power);
		m1.forward();
		m2.backward();
		this.power = power;
	}

	public int getPower() {
		return this.power;
	}

	/**
	 * Drive at max power.
	 */
	public void forward() {
		forward(1000);
	}

	/**
	 * Drive backward with the given power.
	 * 
	 * @param power
	 */
	public void backward(int power) {
		forward(-power);
	}

	/**
	 * Backward at max speed.
	 */
	public void backward() {
		forward(-1000);
	}

	public void stop() {
		m1.stop();
		m2.stop();
	}

	public void setPower(int power) {
		m1.setPower(power);
		m2.setPower(power);
	}

	public void turnLeft(int power) {
		this.setPower(power);
		m1.forward();
		m2.forward();
	}

	public void turnRight(int power) {
		this.setPower(power);
		m1.backward();
		m2.backward();
	}

}
