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
 * TODO: What is this class and why do we have it?
 *
 */
public class Start {

	private static final String A_NAME = "SDP 9A", A_MAC = "0016530BB5A3",
			B_NAME = "SDP 9B", B_MAC = "001653077531";

	public static void main(String[] args) throws Exception {
		ServerSocket serverSocket = null;
		NXTInfo nxt = null;
		NXTInfo defender = new NXTInfo(NXTCommFactory.BLUETOOTH, B_NAME, B_MAC);
		NXTInfo attacker = new NXTInfo(NXTCommFactory.BLUETOOTH, A_NAME, A_MAC);
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
			Exception e = new Exception("Couldn't select a robot...");
			throw e;
		}
		boolean work = true;

		BTConnection conn1 = new BTConnection(nxt, NXTComm.PACKET);
		while (work) {

			try {
				String input = "";
				serverSocket = new ServerSocket(port);
				// If you get an "Address already in use" error, change the
				// port in Constants to some other 4-digit number above 1024
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
							double dist = Double.parseDouble(fromClient
									.readLine());
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
