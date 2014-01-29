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
import sdp.pc.robot.btcomm.BTConnection;

public class Start {
	private static final String A_NAME = "SDP 9A", A_MAC = "0016530BB5A3",
			B_NAME = "SDP 9B", B_MAC = "001653077531";
			
	public static void main(String[] args) {
		NXTInfo nxt1 = new NXTInfo(NXTCommFactory.BLUETOOTH, B_NAME, B_MAC);

		//NXTInfo nxt1 = new NXTInfo(NXTCommFactory.BLUETOOTH, A_NAME,A_MAC);

		BTConnection conn1 = new BTConnection(nxt1, NXTComm.PACKET);

		try {
			int serverPort = 4456;
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(serverPort);

			while (true) {
				System.out.println("Waiting for client on port "
						+ serverSocket.getLocalPort() + "...");

				Socket server = serverSocket.accept();
				System.out.println("Just connected to "
						+ server.getRemoteSocketAddress());

//				PrintWriter toClient = new PrintWriter(
//						server.getOutputStream(), true);
				BufferedReader fromClient = new BufferedReader(
						new InputStreamReader(server.getInputStream()));
				String line = "";
				while (!"quit".equalsIgnoreCase(line)) {
					char c = fromClient.readLine().charAt(0);
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

	}
}
