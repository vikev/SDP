package sdp.pc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import sdp.pc.common.ChooseRobot;
import sdp.pc.common.Constants;
import sdp.pc.robot.btcomm.BTConnection;

/**
 * This is the class which is run when the ant buildfile is run. You can swap
 * which brick 'a' and 'd' refers to by exchanging 'attacker' and 'defender'
 * below.
 * 
 * If you get an "Address already in use" error, change the port in Constants to
 * some other 4-digit number above 1024
 */
public class Start {

	/**
	 * The device name by which to refer to brick A
	 */
	private static final String A_NAME = "SDP 9A";

	/**
	 * The device MAC address by which to refer to brick A
	 */
	private static final String A_MAC = "0016530BB5A3";

	/**
	 * The device name by which to refer to brick B
	 */
	private static final String B_NAME = "SDP 9B";

	/**
	 * The device MAC address by which to refer to brick B
	 */
	private static final String B_MAC = "001653077531";

	/**
	 * Main method run when the ant buildfile is finished, which selects a robot
	 * and connects it. Also has a few other features implemented by Lubo
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Connect a robot
		ServerSocket serverSocket = null;
		NXTInfo nxt = null;
		NXTInfo attacker = new NXTInfo(NXTCommFactory.BLUETOOTH, A_NAME, A_MAC);
		NXTInfo defender = new NXTInfo(NXTCommFactory.BLUETOOTH, B_NAME, B_MAC);
		int port;

		switch (ChooseRobot.dialog()) {
		case Constants.ATTACKER:
			nxt = attacker;
			port = Constants.ATTACKER_PORT;
			break;
		case Constants.DEFENDER:
			nxt = defender;
			port = Constants.DEFENDER_PORT;
			break;
		default:
			throw new Exception("Couldn't select a robot...");
		}
		BTConnection conn1 = new BTConnection(nxt, NXTComm.PACKET);

		// Persist the connection
		boolean work = true;
		while (work) {
			try {
				String input = "";
				serverSocket = new ServerSocket(port);
				while (true) {
					if ("quit".equalsIgnoreCase(input)) {
						break;
					}
					System.out.println("Waiting for client on port "
							+ serverSocket.getLocalPort() + "...");

					Socket server = serverSocket.accept();
					System.out.println("Just connected to "
							+ server.getRemoteSocketAddress());

					BufferedReader fromClient = new BufferedReader(
							new InputStreamReader(server.getInputStream()));
					try {
						while (true) {
							input = fromClient.readLine();
							if ("close".equalsIgnoreCase(input)) {
								fromClient.close();
								break;
							}
							if ("quit".equalsIgnoreCase(input)) {
								fromClient.close();
								work = false;
								break;
							}
							char c = input.charAt(0);
							int dist = Integer.parseInt(fromClient.readLine());
							conn1.sendCommand(c, dist);
							System.out.println("Command sent to the robot: "
									+ c + " " + dist);
						}
					} catch (Exception e) {
						// whatever goes wrong wait for a new connection
						System.err.println(e);
						System.out.println("Socket would restart now");
					}
					// stop the robot if the connection is dropped or stopped
					conn1.sendCommand('s', 0);
				}
			} catch (UnknownHostException ex) {
				ex.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.err.println("Couldn't close the socket.");
				e.printStackTrace();
			}
		}
		conn1.disconnect();
	}
}
