package sdp.nxt.interfaces;

public interface Pilot {

	/**
	 * The power is percentage. 0-100
	 */
	public void forward(int power);

	public int getPower();

	/**
	 * Drive at max power.
	 */
	public void forward();

	/**
	 * Drive backward with the given power.
	 * 
	 * @param power
	 */
	public void backward(int power);

	/**
	 * Backward at max speed.
	 */
	public void backward();

	public void stop();

	public void setPower(int power);

	public void turnLeft(int power);

	public void turnRight(int power);

}
