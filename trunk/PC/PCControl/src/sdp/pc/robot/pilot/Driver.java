package sdp.pc.robot.pilot;

import sdp.pc.robot.btcomm.BTConnection;

/**
 * Class for controlling a specific NXJ Brick via a BTConnection.
 */
public class Driver {

	/**
	 * The warning which is displayed when a Driver is instantiated when the
	 * connection is not ready.
	 */
	private static final String NOT_CONNECTED = "Warning: Tried to instantiate a Driver without a connected bluetooth connection";

	/**
	 * The bluetooth connection associated with this brick
	 */
	private BTConnection conn;

	/**
	 * The only constructor for a Driver requires an already-opened bluetooth
	 * connection
	 * 
	 * @param conn
	 */
	public Driver(BTConnection conn) {
		if (!conn.isConnected()) {
			System.out.println(NOT_CONNECTED);
		}
		this.conn = conn;
	}

	/**
	 * Travel forward with the specified speed.
	 * 
	 * @param speed
	 *            The speed at which to move. TODO: Describe units (depends on
	 *            gear ratio?), usual values, what happens with negative, zero,
	 *            very large values
	 */
	public boolean forward(double speed) {
		return conn.sendCommand('f', speed);
	}

	/**
	 * Travel forward with speed parameter zero. TODO: What does a speed
	 * parameter of 0 do?
	 */
	public boolean forward() {
		return conn.sendCommand('f', 0);
	}

	/**
	 * Travel backward with the specified speed.
	 * 
	 * @param speed
	 *            The speed at which to move. TODO: Describe units (depends on
	 *            gear ratio?), usual values, what happens with negative, zero,
	 *            very large values
	 */
	public boolean backward(double dist) {
		return conn.sendCommand('b', dist);
	}

	/**
	 * Travel backward with speed parameter zero. TODO: What does a speed
	 * parameter of 0 do?
	 */
	public boolean backward() {
		return conn.sendCommand('b', 0);
	}

	/**
	 * Turn left at specified speed. TODO: What does speed represent? What does
	 * 0 do? What does a negative number do?
	 * 
	 * @param speed
	 * @return
	 */
	public boolean turnLeft(double speed) {
		return conn.sendCommand('l', speed);
	}

	/**
	 * Turn right at specified speed. TODO: What does speed represent? What does
	 * 0 do? What does a negative number do?
	 * 
	 * @param speed
	 * @return
	 */
	public boolean turnRight(double speed) {
		return conn.sendCommand('r', speed);
	}

	/**
	 * Kick the ball at the given speed. TODO: Does the speed parameter work?
	 * What are the limits/units?
	 * 
	 * @param speed
	 * @return
	 */
	public boolean kick(double speed) {
		return conn.sendCommand('k', speed);
	}

	/**
	 * Makes the robot instantly stop moving/turning.
	 * 
	 * @return
	 */
	public boolean stop() {
		return conn.sendCommand('s', 0);
	}
}
