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
 * Strategy is the global static class for running a match in SDP. Here we
 * initialise a Vision and Robot pair, and conduct all the state-based decision
 * model related data to command the robot pair. Then we iterate several times
 * per second, parsing the game state and ordering the robots to do things.
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

	private JButton button = null;

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

	private static int guiState = 0;

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

	public Strategy() throws InterruptedException {
		this.startVisionSystem();

		// Add a start button so we can have time to calibrate etc
		JFrame frame = new JFrame("Ready and waiting!");
		ImageIcon q = new ImageIcon("resource/strategy.png");
		frame.setIconImage(q.getImage());
		frame.setBounds(600, 200, 300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		button = new JButton();
		button.setText("Connect");
		button.addActionListener(this);
		frame.add(button);
		frame.setVisible(true);
	}
	
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
				button.setText("Start");

				guiState = 1;
			} else if (guiState == 1) {
				// Begin looping through the strategy functionality
				// executeStrategy();
				instance = new StrategyThread(attacker, defender, state);
				instance.start();
				button.setText("Pause");
				guiState = 2;
			} else if (guiState == 2) {
				instance.interrupt();
				button.setText("Restart");
				guiState = 1;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Main method
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

	public WorldState getState() {
		return this.state;
	}
}
