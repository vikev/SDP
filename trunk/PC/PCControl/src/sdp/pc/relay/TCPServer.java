package sdp.pc.relay;

import java.net.*;
import java.io.*;

import sdp.pc.common.Constants;

public class TCPServer {
	public void run() {
		ServerSocket serverSocket = null;
		try {
			int serverPort = 4020;
			serverSocket = new ServerSocket(serverPort);

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
		finally {
			//f*k java
			if(serverSocket != null)
				//try in a finally
				try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public static void main(String[] args) {
		TCPServer srv = new TCPServer();
		srv.run();
	}
}