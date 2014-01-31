package sdp.pc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import sdp.pc.common.Constants;
import sdp.pc.robot.btcomm.BTConnection;

public class Start {

	@SuppressWarnings("unused")
	private static final String A_NAME = "SDP 9A", A_MAC = "0016530BB5A3",
			B_NAME = "SDP 9B", B_MAC = "001653077531";

	public static void main(String[] args) {

		ServerSocket serverSocket = null;
		// NXTInfo nxt1 = new NXTInfo(NXTCommFactory.BLUETOOTH, B_NAME, B_MAC);

		NXTInfo nxt1 = new NXTInfo(NXTCommFactory.BLUETOOTH, A_NAME, A_MAC);

		BTConnection conn1 = new BTConnection(nxt1, NXTComm.PACKET);

		try {
			serverSocket = new ServerSocket(Constants.PORT);
			// If you get an "Address already in use" error, change the
			// port in Constants to some other 4-digit number above 1024
			String input = "";
			while (true) {
				if ("quit".equalsIgnoreCase(input)) {
					break;
				}
				;
				System.out.println("Waiting for client on port "
						+ serverSocket.getLocalPort() + "...");

				Socket server = serverSocket.accept();
				System.out.println("Just connected to "
						+ server.getRemoteSocketAddress());

				BufferedReader fromClient = new BufferedReader(
						new InputStreamReader(server.getInputStream()));
				while (true) {
					input = fromClient.readLine();
					if ("quit".equalsIgnoreCase(input)) {
						fromClient.close();
						break;
					}
					char c = input.charAt(0);
					double dist = Double.parseDouble(fromClient.readLine());
					conn1.sendCommand(c, dist);
				}
			}
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		conn1.disconnect();
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("Couldn't close the socket.");
			e.printStackTrace();
		}

	}
}
