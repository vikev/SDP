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
		switch (code) {

		// Left Arrow: Turn Left
		case KeyCode.LEFT_ARROW:
			try {
				tcpClient.sendCommand((byte) 3, 50);
			} catch (Exception e7) {
				// TODO Auto-generated catch block
				e7.printStackTrace();
			}
			break;

		// Up Arrow: Move Forward
		case KeyCode.UP_ARROW:
			try {
				tcpClient.sendCommand((byte) 1, 50);
			} catch (Exception e6) {
				// TODO Auto-generated catch block
				e6.printStackTrace();
			}
			break;

		// Right Arrow: Turn Right
		case KeyCode.RIGHT_ARROW:
			try {
				tcpClient.sendCommand((byte) 4, 50);
			} catch (Exception e5) {
				// TODO Auto-generated catch block
				e5.printStackTrace();
			}
			break;

		// Down Arrow: Move Backward
		case KeyCode.DOWN_ARROW:
			try {
				tcpClient.sendCommand((byte) 2, 50);
			} catch (Exception e4) {
				// TODO Auto-generated catch block
				e4.printStackTrace();
			}
			break;

		// s: Stop
		case KeyCode.S:
			try {
				tcpClient.sendCommand((byte) 5, 50);
			} catch (Exception e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			break;

		// Space bar: Kick
		case KeyCode.SPACE_BAR:
			try {
				tcpClient.sendCommand((byte) 6, 5000);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			break;

		// g: Grab
		case KeyCode.G:
			try {
				tcpClient.sendCommand((byte) 7, 0);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
