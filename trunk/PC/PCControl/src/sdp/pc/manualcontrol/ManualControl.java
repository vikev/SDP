package sdp.pc.manualcontrol;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;

import sdp.pc.common.ChooseRobot;
import sdp.pc.common.Constants;

/**
 * ManualControl is a class for connecting to a robot and sending arbitrary
 * commands, for testing the function of the robot and receiver.
 * 
 * <ul>
 * <li>[ArrowKeyLeft] -> Turn Left</li>
 * <li>[ArrowKeyUp] -> Move Forwards</li>
 * <li>[ArrowKeyRight] -> Turn Right</li>
 * <li>[ArrowKeyDown] -> Move Backwards</li>
 * <li>s -> Stop</li>
 * <li>[SpaceBar] -> Kick</li>
 * <li>g -> Grab</li>
 * </ul>
 * 
 * TODO: Test and make sure the refactorings on March 3rd didn't break anything.
 * 
 */
public class ManualControl implements KeyListener {

	/**
	 * The object for sending commands to the connection
	 */
	private PrintWriter toServer;

	/**
	 * The port under which the robot is connected
	 */
	private final int serverPort;

	/**
	 * Simple constructor for a manual control instance
	 * 
	 * @param serverPort
	 */
	private ManualControl(int serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * Begins listening to key commands and sending robot data.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		InetAddress host = InetAddress.getByName(Constants.HOST);
		System.out.println("Connecting to server on port " + serverPort);
		Socket socket = new Socket(host, serverPort);
		// Socket socket = new Socket("127.0.0.1", serverPort);
		System.out.println("Just connected to "
				+ socket.getRemoteSocketAddress());
		toServer = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader fromServer = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		String input = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		JFrame frame = new JFrame("control");
		frame.setBounds(50, 50, 300, 300);
		frame.addKeyListener(this);
		frame.setVisible(true);
		while (!"quit".equalsIgnoreCase(input)) {
			input = br.readLine();
			toServer.println(input);
		}
		toServer.close();
		fromServer.close();
		socket.close();
	}

	/**
	 * The main method which is run, loading up a key listener for sending the
	 * robot commands
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Connect to a robot
		int port;
		switch (ChooseRobot.dialog()) {
		case Constants.ATTACKER:
			port = Constants.ATTACKER_PORT;
			break;
		case Constants.DEFENDER:
			port = Constants.DEFENDER_PORT;
			break;
		default:
			Exception e = new Exception("Couldn't select a robot...");
			throw e;
		}

		// Run a manual control instance with the connected robot
		new ManualControl(port).run();
	}

	/**
	 * The implemented key-pressed event, for sending commands to the robot.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		System.out.println(code);

		// Send a command depending on the key code
		switch (code) {

		// Left Arrow: Turn Left
		case KeyCode.LEFT_ARROW:
			toServer.println("l");
			toServer.println(50);
			break;

		// Up Arrow: Move Forward
		case KeyCode.UP_ARROW:
			toServer.println("f");
			toServer.println(50);
			break;

		// Right Arrow: Turn Right
		case KeyCode.RIGHT_ARROW:
			toServer.println("r");
			toServer.println(50);
			break;

		// Down Arrow: Move Backward
		case KeyCode.DOWN_ARROW:
			toServer.println("b");
			toServer.println(50);
			break;

		// s: Stop
		case KeyCode.S:
			toServer.println("s");
			toServer.println(50);
			break;

		// Space bar: Kick
		case KeyCode.SPACE_BAR:
			toServer.println("k");
			toServer.println(2222);
			break;

		// g: Grab
		case KeyCode.G:
			toServer.println("g");
			toServer.println(2222);
			break;
		}
	}

	/**
	 * Unused, but required implemented keyReleased event
	 */
	@Override
	public void keyReleased(KeyEvent e) {
	}

	/**
	 * Unused, but required implemented keyTyped event
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * A private class (like an enum) for referencing key codes
	 */
	private class KeyCode {
		/**
		 * The key code value for referencing a keyboard left arrow
		 */
		public static final int LEFT_ARROW = 37;

		/**
		 * The key code value for referencing a keyboard up arrow
		 */
		public static final int UP_ARROW = 38;

		/**
		 * The key code value for referencing a keyboard right arrow
		 */
		public static final int RIGHT_ARROW = 39;

		/**
		 * The key code value for referencing a keyboard down arrow
		 */
		public static final int DOWN_ARROW = 40;

		/**
		 * The key code value for referencing a keyboard s button
		 */
		public static final int S = 83;

		/**
		 * The key code value for referencing a keyboard space bar
		 */
		public static final int SPACE_BAR = 32;

		/**
		 * The key code value for referencing a keyboard g button
		 */
		public static final int G = 71;
	}
}
