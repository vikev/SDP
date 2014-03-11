package sdp.pc.vision.relay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import sdp.pc.common.Constants;

/**
 * TCPClient is the class used by our bluetooth relay/receiver for ensuring
 * commands.
 */
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
	
	private String lastCmdString;
	
	private boolean filterCommand(char command, int arg) {
		String cmdString = Character.toString(command) + Integer.toString(arg);
		String filteredCommands = "fblrs";
		boolean filter = filteredCommands.indexOf(command) > 0 && cmdString == lastCmdString;
		lastCmdString = cmdString;
		return filter;
	}
	
	public boolean sendCommand(char command, int arg) throws Exception {
		if (connected && !filterCommand(command, arg)) {
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