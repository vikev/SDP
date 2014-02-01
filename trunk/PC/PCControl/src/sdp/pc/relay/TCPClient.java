package sdp.pc.relay;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;

import javax.swing.JFrame;

import sdp.pc.common.Constants;

public class TCPClient {
	public void run() {

		try {
			int serverPort = Constants.PORT;
			InetAddress host = InetAddress.getByName(Constants.HOST);
			System.out.println("Connecting to server on port " + serverPort);

			Socket socket = new Socket(host, serverPort);
			// Socket socket = new Socket("127.0.0.1", serverPort);
			System.out.println("Just connected to "
					+ socket.getRemoteSocketAddress());
			final PrintWriter toServer = new PrintWriter(
					socket.getOutputStream(), true);
			BufferedReader fromServer = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String input = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			JFrame frame = new JFrame("control");
			frame.setBounds(50, 50, 300, 300);
			frame.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					int code = e.getKeyCode();
					System.out.println(code);
					if (code == 37) {
						toServer.println("l");
						toServer.println(10);
					}
					if (code == 38) {
						toServer.println("f");
						toServer.println(0);
					}
					if (code == 39) {
						toServer.println("r");
						toServer.println(10);
					}
					if (code == 40) {
						toServer.println("b");
						toServer.println(0);
					}
					if(code==83){
						toServer.println("s");
						toServer.println(0);
					}
					if(code==32){
						toServer.println("p");
						toServer.println(0);
					}
				}

				public void keyReleased(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
					toServer.println("s");
					toServer.println(0);
				}
			});
			frame.setVisible(true);
			while (!"quit".equalsIgnoreCase(input)) {
				input = br.readLine();
				toServer.println(input);
			}

			toServer.close();
			fromServer.close();
			socket.close();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TCPClient client = new TCPClient();
		client.run();
	}
}