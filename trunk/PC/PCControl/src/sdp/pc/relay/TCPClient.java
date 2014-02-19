package sdp.pc.relay;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import sdp.pc.common.ChooseRobot;
import sdp.pc.common.Constants;

/**
 * TODO: What is this class for and are we using it?
 *
 */
public class TCPClient {

	public void run(int serverPort, final int robot) {

		try {
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
					/* Turn left - arrow left */
					if (code == 37) {
						toServer.println("l");
						toServer.println(0);
					}
					/* Forwards - arrow up */
					if (code == 38) {
						toServer.println("f");
						toServer.println(0);
					}
					/* Turn right - arrow right */
					if (code == 39) {
						toServer.println("r");
						toServer.println(0);
					}/* Backwards - arrow down */
					if (code == 40) {
						toServer.println("b");
						toServer.println(0);
					}/* Stop when s is pressed */
					if (code == 83) {
						toServer.println("s");
						toServer.println(0);
					}
					/* Kcik - spacebar */
					if (code == 32) {
						switch (robot) {
						case Constants.ATTACKER:
							toServer.println("p");
							break;
						case Constants.DEFENDER:
							toServer.println("k");
							break;
						}
						toServer.println(2222);
					}
				}

				public void keyReleased(KeyEvent e) {

				}

				public void keyTyped(KeyEvent e) {
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

	public static void main(String[] args) throws Exception {
		int port;
		int robot;
		switch (ChooseRobot.dialog()) {
		case Constants.ATTACKER:
			robot = Constants.ATTACKER;
			port = Constants.ATTACKER_PORT;
			break;
		case Constants.DEFENDER:
			port = Constants.DEFENDER_PORT;
			robot = Constants.DEFENDER;
			break;
		default:
			Exception e = new Exception("Couldn't select a robot...");
			throw e;
		}
		TCPClient client = new TCPClient();
		client.run(port, robot);
	}
}