package sdp.pc.robot.pilot;

import sdp.pc.robot.btcomm.BTConnection;

public class Driver {
	BTConnection conn;

	/**
	 * Needs and open bt connection to the brick.
	 * 
	 * @param conn
	 */
	public Driver(BTConnection conn) {
		this.conn = conn;
	}

	/**
	 * Travel farward that distance. 0 = travel forever.
	 * 
	 * @param dist
	 */
	public boolean forward(double dist) {
		return conn.sendCommand('f', dist);
	}

	/**
	 * Travel farward forever.
	 */
	public boolean forward() {
		return conn.sendCommand('f', 0);
	}

	/**
	 * Travel backward that distance. 0 = travel forever.
	 * 
	 * @param dist
	 */
	public boolean backward(double dist) {
		return conn.sendCommand('b', dist);
	}

	/**
	 * Travel backward forever.
	 */
	public boolean backward() {
		return conn.sendCommand('b', 0);
	}

	/**
	 * Turn left by given angle in degrees.
	 * 
	 * @param deg
	 * @return
	 */
	public boolean turnLeft(double deg) {
		return conn.sendCommand('l', deg);
	}

	/**
	 * Turn right by given angle in degrees.
	 * 
	 * @param deg
	 * @return
	 */
	public boolean turnRight(double deg) {
		return conn.sendCommand('r', deg);
	}

	/**
	 * Kick the ball at hopefully the given distance.
	 * 
	 * @param dist
	 * @return
	 */
	public boolean kick(double dist) {
		return conn.sendCommand('k', dist);
	}

	/**
	 * Robot stop whatever you are doing.
	 * 
	 * @return
	 */
	public boolean stop() {
		return conn.sendCommand('s', 0);
	}
}