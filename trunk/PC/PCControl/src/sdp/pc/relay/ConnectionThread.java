package sdp.pc.relay;

import java.io.IOException;
import java.io.InputStream;
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
		while (true) {
			try {
				serverSocket = new ServerSocket(port);
				while (true) {
					System.out.println("Waiting for client on port "
							+ serverSocket.getLocalPort() + "...");
					Socket server = serverSocket.accept();
					System.out.println("Just connected to "
							+ server.getRemoteSocketAddress());

					InputStream is = server.getInputStream();

					while (true) {
						try {
							byte[] data = new byte[3];
							if (is.read(data) == 3) {
								if (conn1.isConnected()) {
									conn1.sendCommand(data);
								} else {
									System.out.println("Not connected");
								}
							} else {
								break;
							}
						} catch (Exception e) {
							// TODO
							System.out.println("Couldn't send command");
							e.printStackTrace();
							break;
						}
					}

					// stop the robot if the connection is dropped or stopped
					conn1.sendCommand(new byte[] { 5, 0, 0 });

				}
			} catch (Exception e) {
				try {
					serverSocket.close();
				} catch (IOException e1) {
					// nothing
				}
				System.out.println("Restarting...");
			}
		}
	}
}
