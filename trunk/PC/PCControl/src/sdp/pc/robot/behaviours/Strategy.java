package sdp.pc.robot.behaviours;

import javax.swing.SwingUtilities;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import sdp.pc.common.ChooseRobot;
import sdp.pc.robot.arbitrators.Arbitrator;
import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;

public class Strategy {
	
	/**
	 * Team ID of our team
	 * TODO abstract
	 */
	private static final int MY_TEAM = 1;

	/**
	 * ID for the defending robot
	 * TODO abstract
	 */
	private static final int DEFENDER_ID = 0;

	/**
	 * ID for the attacking robot
	 * TODO abstract
	 */
	private static final int ATTACKER_ID = 1;

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
		// TODO: Having two different versions of TCPClient and Driver is
		// extremely difficult to deal with. Can someone familiar with both
		// classes refactor/remove something please
		defender = new Robot(ChooseRobot.defender(), state, MY_TEAM,
				DEFENDER_ID);
		attacker = new Robot(ChooseRobot.attacker(), state, MY_TEAM,
				ATTACKER_ID);
	}
	
	public static void main(String [] args) throws Exception {
		//Sorry Yordan, copied stuff from another strategy and haven't
		//done much yet and need to go now :D
		startVisionSystem();
		connectRobots();
		Behavior b1 = new MoveNearGoals(defender);
		Behavior b2 = new TurnPerpendicular(defender);
		Behavior b3 = new DefendBall(defender);
		Behavior b4 = new DefendRobot(defender);
		Behavior [] bArray = {b1, b2, b3, b4};
		Arbitrator arby = new Arbitrator(bArray);
		arby.start();
   }
}