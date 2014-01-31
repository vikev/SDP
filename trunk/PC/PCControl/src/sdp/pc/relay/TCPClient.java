package sdp.pc.relay;

import java.io.*;
import java.net.*;

import sdp.pc.common.Constants;

public class TCPClient {
  public void run() {
	try {
		int serverPort = Constants.PORT;
		InetAddress host = InetAddress.getByName(Constants.HOST); 
		System.out.println("Connecting to server on port " + serverPort); 

		Socket socket = new Socket(host,serverPort); 
		//Socket socket = new Socket("127.0.0.1", serverPort);
		System.out.println("Just connected to " + socket.getRemoteSocketAddress()); 
		PrintWriter toServer = 
			new PrintWriter(socket.getOutputStream(),true);
		BufferedReader fromServer = 
			new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
		String input = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while(!"quit".equalsIgnoreCase(input)){
			input = br.readLine();
			toServer.println(input);
		}
		
		
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