package sdp.pc.relay;

import java.io.*;
import java.net.*;

public class TCPClient {
  public void run() {
	try {
		int serverPort = 4020;
		InetAddress host = InetAddress.getByName("localhost"); 
		System.out.println("Connecting to server on port " + serverPort); 

		Socket socket = new Socket(host,serverPort); 
		//Socket socket = new Socket("127.0.0.1", serverPort);
		System.out.println("Just connected to " + socket.getRemoteSocketAddress()); 
		PrintWriter toServer = 
			new PrintWriter(socket.getOutputStream(),true);
		BufferedReader fromServer = 
			new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
		toServer.println("Hello from " + socket.getLocalSocketAddress()); 

		System.out.println("Client received: " + fromServer.readLine() + " from Server");
		System.out.println("Client received: " + fromServer.readLine() + " from Server");
		
		toServer.close();
		fromServer.close();
		socket.close();
	}
	catch(UnknownHostException ex) {
		ex.printStackTrace();
	}
	catch(IOException e){
		e.printStackTrace();
	}
  }
	
  public static void main(String[] args) {
		TCPClient client = new TCPClient();
		client.run();
  }
}