package sdp.pc.relay;

import java.net.*;
import java.io.*;

public class TCPServer {
	public void run() {
		try {
			int serverPort = 4020;
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(serverPort);

			while (true) {
				System.out.println("Waiting for client on port "
						+ serverSocket.getLocalPort() + "...");

				Socket server = serverSocket.accept();
				System.out.println("Just connected to "
						+ server.getRemoteSocketAddress());

				@SuppressWarnings("unused")
				PrintWriter toClient = new PrintWriter(
						server.getOutputStream(), true);
				BufferedReader fromClient = new BufferedReader(
						new InputStreamReader(server.getInputStream()));
				String line = "";
				while (!"quit".equalsIgnoreCase(line)) {
					fromClient.readLine();
					System.out.println("Server received: " + line);
				}
			}
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TCPServer srv = new TCPServer();
		srv.run();
	}
}