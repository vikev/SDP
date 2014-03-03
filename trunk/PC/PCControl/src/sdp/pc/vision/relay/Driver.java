package sdp.pc.vision.relay;

/**
 * A driver is an abstract controller for a robot. Drivers send basic commands
 * to an NXT brick which are parsed by the brick's receiver.
 */
public class Driver {
	private TCPClient conn;
	private int robotId;

	/**
	 * Needs and open bt connection to the brick.
	 * 
	 * @param conn
	 */
	public Driver(TCPClient conn) {
		this.conn = conn;
		this.robotId = conn.getRobotId();
	}

	public void closeConnection() {
		conn.closeConnection();
	}

	/**
	 * Travel forward at that speed. 0 = travel at max speed.
	 * 
	 * @param dist
	 * @throws Exception
	 */
	public boolean forward(int power) throws Exception {
		return conn.sendCommand('f', power);
	}

	/**
	 * Travel forward at maximum speed.
	 * 
	 * @throws Exception
	 */
	public boolean forward() throws Exception {
		return forward(0);
	}

	/**
	 * Travel backward at that speed. 0 = travel at max speed.
	 * 
	 * @param dist
	 * @throws Exception
	 */
	public boolean backward(int power) throws Exception {
		return conn.sendCommand('b', power);
	}

	/**
	 * Travel backward at maximum speed.
	 * 
	 * @throws Exception
	 */
	public boolean backward() throws Exception {
		return backward(0);
	}

	/**
	 * Turn left with that speed. 0 = travel at max speed.
	 * 
	 * @param deg
	 * @return
	 * @throws Exception
	 */
	public boolean turnLeft(int deg) throws Exception {
		return conn.sendCommand('l', deg);
	}

	/**
	 * Turn left until interrupted at max speed.
	 * 
	 * @param deg
	 * @return
	 * @throws Exception
	 */
	public boolean turnLeft() throws Exception {
		return turnLeft(0);
	}

	/**
	 * Turn right with that speed.
	 * 
	 * @param deg
	 * @return
	 * @throws Exception
	 */
	public boolean turnRight(int deg) throws Exception {
		return conn.sendCommand('r', deg);
	}

	/**
	 * Turn right with maximum speed.
	 * 
	 * @param deg
	 * @return
	 * @throws Exception
	 */
	public boolean turnRight() throws Exception {
		return turnRight(0);
	}

	/**
	 * Kick the ball.
	 * 
	 * @param dist
	 *            For the attacker this would be disregarded. For the defender
	 *            it controlls the rotation speed of the kicker motor.
	 * @return
	 * @throws Exception
	 */
	public boolean kick(int power) throws Exception {
		return conn.sendCommand('k', power);
	}

	/**
	 * Close the grabber to grab the ball
	 * 
	 * @param speed
	 * @return
	 * @throws Exception
	 */
	public boolean grab() throws Exception {
		return conn.sendCommand('g', 0);
	}

	/**
	 * Robot please stop whatever you are doing.
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean stop() throws Exception {
		return conn.sendCommand('s', 0);
	}

	public boolean setSpeed(int power) throws Exception {
		return conn.sendCommand('p', power);
	}

	/**
	 * Returns the robot's id: Either Constants.ATTACKER or Constants.DEFENDER
	 */
	public int getRobotId() {
		return robotId;
	}
}
