package sdp.pc.vision.relay;

import java.io.*;
import java.net.*;

import sdp.pc.common.Constants;

public class TCPClient {
	private PrintWriter toServer;
	private BufferedReader fromServer;
	private Socket socket;
	private boolean connected = false;
	private String serverAddress;
	private int serverPort;
	private int robotId;

	/**
	 * @param robotId
	 *            Id of the robot - either Constants.ATTACKER or
	 *            Constants.DEFENDER
	 * @throws Exception
	 *             Thrown if the id is wrong.
	 */
	public TCPClient(int robotId) throws Exception {
		this.robotId = robotId;
		switch (robotId) {
		case Constants.ATTACKER:
			this.serverAddress = Constants.HOST;
			this.serverPort = Constants.ATTACKER_PORT;
			break;
		case Constants.DEFENDER:
			this.serverAddress = Constants.HOST;
			this.serverPort = Constants.DEFENDER_PORT;
			break;
		default:
			Exception up = new Exception("Wrong input parameter(robot id)");
			throw up;
		}
	}

	public boolean connect() {
		try {
			InetAddress host = InetAddress.getByName(serverAddress);
			System.out.println("Connecting to server on port " + serverPort);

			socket = new Socket(host, serverPort);
			System.out.println("Just connected to "
					+ socket.getRemoteSocketAddress());
			toServer = new PrintWriter(socket.getOutputStream(), true);
			fromServer = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			connected = true;
			return true;
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
			connected = false;
		} catch (IOException e) {
			e.printStackTrace();
			connected = false;
		}
		return false;
	}

	public boolean sendCommand(char command, double arg) throws Exception {
		if (connected) {
			toServer.println(command);
			toServer.println(arg);
			return true;
		} else {
			if (connect()) {
				return sendCommand(command, arg);
			} else {
				Exception e = new Exception(
						"Couldn't establish a connection to " + serverAddress
								+ ":" + serverPort + "!!!");
				throw (e);
			}
		}
	}

	public void closeConnection() {
		try {
			toServer.close();
			fromServer.close();
			socket.close();
		} catch (IOException e) {
			System.err.print("Couldn't close the connection to "
					+ serverAddress + ":" + serverPort + "!!!");
			e.printStackTrace();
		}
		connected = false;
	}

	/**
	 * Returns the robot's id: Either Constants.ATTACKER or Constants.DEFENDER
	 */
	public int getRobotId() {
		return robotId;
	}
}