package sdp.pc.robot.pilot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import sdp.pc.common.ChooseRobot;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

/**
 * Strategy is the class we use to launch a match of SDP. It starts a vision
 * instance and builds the GUI which can be used to start and interrupt an
 * instance of StrategyThread.
 * 
 * @author s1133141
 * 
 */
public class Strategy implements ActionListener {

	/**
	 * Team ID of our team (could be refactored?)
	 */
	private static int myTeam;

	/**
	 * ID for the defending robot (could be refactored?)
	 */
	private static int defenderId;

	/**
	 * ID for the attacking robot (could be refactored?)
	 */
	private static int attackerId;

	/**
	 * The button used to start/pause StrategyThread
	 */
	private JButton startButton = null;

	/**
	 * the defending robot
	 */
	private static Robot defender;

	/**
	 * the attacking robot
	 */
	private static Robot attacker;

	/**
	 * global world state (we instantiate it because this is a main class)
	 */
	private WorldState state = new WorldState();

	/**
	 * A state handler for the connect/start button (FSM pattern)
	 */
	private static int guiState = 0;

	/**
	 * The currently running (or not) instance of StrategyThread.
	 */
	private static StrategyThread instance;

	/**
	 * Builds the vision system
	 * 
	 * @throws InterruptedException
	 */
	private void startVisionSystem() throws InterruptedException {
		Thread.sleep(1000);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new Vision(state);
				} catch (V4L4JException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Connects both robots
	 * 
	 * @throws Exception
	 */
	private void connectRobots() throws Exception {
		Thread.sleep(1000);
		defender = new Robot(ChooseRobot.defender(), state, myTeam, defenderId);
		attacker = new Robot(ChooseRobot.attacker(), state, myTeam, attackerId);
	}

	/**
	 * The shutdown hook for a closed thread (shuts off both connections)
	 * 
	 * @throws InterruptedException
	 */
	private static void addShutdownHook() throws InterruptedException {
		Thread.sleep(1000);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Exiting and stopping the robot");
				try {
					attacker.stop();
					defender.stop();
					attacker.closeConnection();
					defender.closeConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("Should have stopped by now.");
			}
		});
	}

	/**
	 * Constructor which turns on the vision system, builds the GUI
	 * 
	 * @throws InterruptedException
	 */
	public Strategy() throws InterruptedException {
		this.startVisionSystem();

		// Add a start button so we can have time to calibrate etc
		JFrame frame = new JFrame("Ready and waiting!");
		ImageIcon q = new ImageIcon("resource/strategy.png");
		frame.setIconImage(q.getImage());
		frame.setBounds(600, 200, 300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		startButton = new JButton();
		startButton.setText("Connect");
		startButton.addActionListener(this);
		frame.add(startButton);
		frame.setVisible(true);
	}

	/**
	 * The click listener for the button which connects, starts, pauses, or
	 * restarts the robots
	 */

	public void actionPerformed(ActionEvent e) {
		try {
			if (guiState == 0) {
				// Set our team and direction
				myTeam = this.getState().getOurColor();
				attackerId = this.getState().getDirection();
				defenderId = 1 - this.getState().getDirection();
				System.out.println("Sanity Check! " + "Our Team: " + myTeam
						+ ", Defender Side: " + defenderId);
				// Connect to robots
				this.connectRobots();
				addShutdownHook();

				// Change the button
				startButton.setText("Start");

				guiState = 1;
			} else if (guiState == 1) {
				// Begin looping through the strategy functionality
				// executeStrategy();
				instance = new StrategyThread(attacker, defender, state);
				instance.start();
				startButton.setText("Pause");
				guiState = 2;
			} else if (guiState == 2) {
				instance.interrupt();
				startButton.setText("Restart");
				guiState = 1;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Main method which simply runs an instance of Strategy.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new Strategy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the world state attached to this Strategy/Vision bundle
	 * 
	 * @return
	 */
	public WorldState getState() {
		return this.state;
	}
}
