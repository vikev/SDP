package sdp.pc.manualcontrol;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;

import sdp.pc.common.ChooseRobot;
import sdp.pc.vision.relay.TCPClient;

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
	int power = 0;
	int lastCode = -1;

	private TCPClient tcpClient;

	/**
	 * The robotID
	 */
	private final int robotID;

	/**
	 * Simple constructor for a manual control instance
	 * 
	 * @param serverPort
	 */
	private ManualControl(int robotID) {
		this.robotID = robotID;
	}

	/**
	 * Begins listening to key commands and sending robot data.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {

		try {
			tcpClient = new TCPClient(robotID);
			tcpClient.connect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Couldn't connect to the relay.");
			return;
		}

		JFrame frame = new JFrame("control");
		frame.setBounds(500, 500, 200, 50);
		frame.setAlwaysOnTop(true);
		JLabel label = new JLabel();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		label.setText("Select and Pess a Key");
		frame.add(label);
		frame.addKeyListener(this);
		frame.setVisible(true);
	}

	/**
	 * The main method which is run, loading up a key listener for sending the
	 * robot commands
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// Run a manual control instance with the selected robot
		new ManualControl(ChooseRobot.dialog()).run();
	}

	/**
	 * The implemented key-pressed event, for sending commands to the robot.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		System.out.println(code);

		// Send a command depending on the key code
		if (code != KeyCode.SPACE_BAR && code != KeyCode.G && code != KeyCode.O
				&& lastCode == code) {
			power += 10;
		} else {
			power = 10;
		}
		lastCode = code;
		switch (code) {

		// Left Arrow: Turn Left
		case KeyCode.LEFT_ARROW:
			tcpClient.sendCommand((byte) 3, power);
			break;

		// Up Arrow: Move Forward
		case KeyCode.UP_ARROW:
			tcpClient.sendCommand((byte) 1, power);
			break;

		// Right Arrow: Turn Right
		case KeyCode.RIGHT_ARROW:
			tcpClient.sendCommand((byte) 4, power);
			break;

		// Down Arrow: Move Backward
		case KeyCode.DOWN_ARROW:
			tcpClient.sendCommand((byte) 2, power);
			break;
		// s: Stop
		case KeyCode.S:
			tcpClient.sendCommand((byte) 5, power);
			break;

		// Space bar: Kick
		case KeyCode.SPACE_BAR:
			tcpClient.sendCommand((byte) 6, 5000);
			break;

		// g: Grab
		case KeyCode.G:
			tcpClient.sendCommand((byte) 7, 0);
			break;
		case KeyCode.O:
			tcpClient.sendCommand((byte) 9, 0);
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

		/**
		 * The key code value for referencing a keyboard o button
		 */
		public static final int O = 79;

	}
}
