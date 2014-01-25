package sdp.pc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.jfree.util.WaitingImageObserver;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import sdp.pc.robot.btcomm.BTConnection;
import sdp.pc.robot.pilot.Driver;

public class Start {
	public static void main(String[] args) {
		// NXTInfo nxt1 = new NXTInfo(NXTCommFactory.BLUETOOTH, "SDP 9B",
		// "001653077531");

		NXTInfo nxt1 = new NXTInfo(NXTCommFactory.BLUETOOTH, "SDP 9A",
				"0016530BB5A3");

		BTConnection conn1 = new BTConnection(nxt1, NXTComm.PACKET);
		
		final Driver driver1 = new Driver(conn1);

		try {
			int serverPort = 4456;
			ServerSocket serverSocket = new ServerSocket(serverPort);

			while (true) {
				System.out.println("Waiting for client on port "
						+ serverSocket.getLocalPort() + "...");

				Socket server = serverSocket.accept();
				System.out.println("Just connected to "
						+ server.getRemoteSocketAddress());

				PrintWriter toClient = new PrintWriter(
						server.getOutputStream(), true);
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
		// conn2.disconnect();

	}
}
