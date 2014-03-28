package sdp.pc.relay;

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

	private InputStream is;

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

				is = server.getInputStream();
				try {
					while (true) {

						try {
							byte[] data = new byte[3];
							is.read(data);
							conn1.sendCommand(data);
						} catch (Exception e) {
							// TODO
							System.out.println("Couldn't send command");
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					// whatever goes wrong wait for a new connection
					System.err.println(e);
					System.out.println("Socket would restart now");
				}
				// stop the robot if the connection is dropped or stopped
				try {
					conn1.sendCommand(new byte[] { 5, 0, 0 });
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
