package sdp.pc.relay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import sdp.pc.common.Constants;

public class ConnectionThread implements Runnable {
	private ServerSocket serverSocket;
	private int port;
	private BTConnection conn1;

	/**
	 * @param connection
	 *            The active connection to the brick
	 * @param robotRole
	 *            defender or attacker
	 */
	public ConnectionThread(BTConnection connection, int robotRole) {
		switch (robotRole) {
		case Constants.ATTACKER:
			port = Constants.ATTACKER_PORT;
			break;
		case Constants.DEFENDER:
			port = Constants.DEFENDER_PORT;
			break;
		}
	}

	public void changeConnection(BTConnection connection) {
		this.conn1 = connection;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
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
						char c = input.charAt(0);
						int dist = Integer.parseInt(fromClient.readLine());
						try {
							conn1.sendCommand(c, dist);
						} catch (Exception e) {
							// TODO
							System.out.println("Couldn't send command");
							e.printStackTrace();
						}
						System.out.println("Command sent to the robot: " + c
								+ " " + dist);
					}
				} catch (Exception e) {
					// whatever goes wrong wait for a new connection
					System.err.println(e);
					System.out.println("Socket would restart now");
				}
				// stop the robot if the connection is dropped or stopped
				try {
					conn1.sendCommand('s', 0);
				} catch (Exception e) {
					// TODO handle this
					System.out.println("couldn't send the command.");
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO: restart somehow?
			e.printStackTrace();
		}
	}
}
