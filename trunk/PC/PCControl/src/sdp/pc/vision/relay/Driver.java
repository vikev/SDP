package sdp.pc.vision.relay;

import sdp.pc.common.Constants;

/**
 * TODO:What is the difference between this and sdp.pc.robot.pilot.Driver ?? We
 * don't need two drivers, I'm quite sure.
 * 
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
	
	public void closeConnection(){
		conn.closeConnection();
	}
	
	/**
	 * Travel forward at that speed. 0 = travel at max speed.
	 * 
	 * @param dist
	 * @throws Exception
	 */
	public boolean forward(double dist) throws Exception {
		return conn.sendCommand('b', dist);
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
	public boolean backward(double dist) throws Exception {
		return conn.sendCommand('f', dist);
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
	public boolean turnLeft(double deg) throws Exception {
		return conn.sendCommand('r', deg);
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
	public boolean turnRight(double deg) throws Exception {
		return conn.sendCommand('l', deg);
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
	public boolean kick(double dist) throws Exception {
		switch (robotId) {
		case Constants.DEFENDER:
			return conn.sendCommand('k', dist);
		case Constants.ATTACKER:
			return conn.sendCommand('p', dist);
		default:
			Exception up = new Exception("Wrong robot id.");
			throw up;
		}
	}
	
	/**
	 * Close the grabber to grab the ball
	 * 
	 * @param speed
	 * @return
	 * @throws Exception 
	 */
	public boolean grab(double speed) throws Exception {
		return conn.sendCommand('g', speed);
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

	/**
	 * Returns the robot's id: Either Constants.ATTACKER or Constants.DEFENDER
	 */
	public int getRobotId() {
		return robotId;
	}
}
