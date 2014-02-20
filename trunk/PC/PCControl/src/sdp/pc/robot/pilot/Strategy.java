package sdp.pc.robot.pilot;

import javax.swing.SwingUtilities;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import sdp.pc.common.ChooseRobot;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;

/**
 * Strategy is the global static class for running a match in SDP. Here we
 * initialise a Vision and Robot pair.
 * 
 * TODO: Better Docu
 * 
 * @author s1133141
 * 
 */
public class Strategy {

	/**
	 * Team ID of our team (could be refactored?)
	 */
	private static final int MY_TEAM = 0;

	/**
	 * ID for the defending robot (could be refactored?)
	 */
	private static final int DEFENDER_ID = 0;

	/**
	 * ID for the attacking robot (could be refactored?)
	 */
	private static final int ATTACKER_ID = 1;

	/**
	 * Controls how often to parse/send commands to the robots. 1/7*1000 = 7
	 * times per second
	 */
	private static final double PERIOD = 1.0 / 7.0 * 1000.0;

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
	private static WorldState state = new WorldState();

	/**
	 * Builds the vision system
	 * 
	 * @throws InterruptedException
	 */
	private static void startVisionSystem() throws InterruptedException {
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
	private static void connectRobots() throws Exception {
		Thread.sleep(1000);
		// TODO: Change this to connect to both robots with a different? dialog.
		// TODO: Having two different versions of TCPClient and Driver is
		// extremely difficult to deal with. Can someone familiar with both
		// classes refactor/remove something please
		TCPClient conn = new TCPClient(ChooseRobot.dialog());
		TCPClient conn2 = new TCPClient(ChooseRobot.dialog());
		Driver driver = new Driver(conn);
		Driver driver2 = new Driver(conn2);
		defender = new Robot(driver, state, MY_TEAM, DEFENDER_ID);
		attacker = new Robot(driver2, state, MY_TEAM, ATTACKER_ID);
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
	 * Main logic branching mechanism for attacker. Use robot.myState as well
	 */
	public static void parseAttacker() {
		// TODO: Logic
	}

	/**
	 * Main logic branching mechanism for defender. Use robot.myState as well
	 */
	public static void parseDefender() {
		// TODO: Logic
	}
	
	/**
	 * Loops indefinitely, ordering the robots to do things
	 * 
	 * @throws InterruptedException
	 */
	public static void executeStrategy() throws InterruptedException {
		Thread.sleep(1000);
		while (true) {
			parseAttacker();
			parseDefender();
			Thread.sleep((int) PERIOD);
		}
	}
	
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			startVisionSystem();
			connectRobots();
			addShutdownHook();
			executeStrategy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
