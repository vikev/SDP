package sdp.pc.vision.relay;

public class Driver {
	TCPClient conn;

	/**
	 * Needs and open bt connection to the brick.
	 * 
	 * @param conn
	 */
	public Driver(TCPClient conn) {
		this.conn = conn;
	}

	/**
	 * Travel farward that distance. 0 = travel forever.
	 * 
	 * @param dist
	 * @throws Exception 
	 */
	public boolean forward(double dist) throws Exception {
		return conn.sendCommand('f', dist);
	}

	/**
	 * Travel farward forever.
	 * @throws Exception 
	 */
	public boolean forward() throws Exception {
		return conn.sendCommand('f', 0);
	}

	/**
	 * Travel backward that distance. 0 = travel forever.
	 * 
	 * @param dist
	 * @throws Exception 
	 */
	public boolean backward(double dist) throws Exception {
		return conn.sendCommand('b', dist);
	}

	/**
	 * Travel backward forever.
	 * @throws Exception 
	 */
	public boolean backward() throws Exception {
		return conn.sendCommand('b', 0);
	}

	/**
	 * Turn left by given angle in degrees.
	 * 
	 * @param deg
	 * @return
	 * @throws Exception 
	 */
	public boolean turnLeft(double deg) throws Exception {
		return conn.sendCommand('l', deg);
	}

	/**
	 * Turn right by given angle in degrees.
	 * 
	 * @param deg
	 * @return
	 * @throws Exception 
	 */
	public boolean turnRight(double deg) throws Exception {
		return conn.sendCommand('r', deg);
	}

	/**
	 * Kick the ball at hopefully the given distance.
	 * 
	 * @param dist
	 * @return
	 * @throws Exception 
	 */
	public boolean kick(double dist) throws Exception {
		return conn.sendCommand('k', dist);
	}

	/**
	 * Robot stop whatever you are doing.
	 * 
	 * @return
	 * @throws Exception 
	 */
	public boolean stop() throws Exception {
		return conn.sendCommand('s', 0);
	}
}
