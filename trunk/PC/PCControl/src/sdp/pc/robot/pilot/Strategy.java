package sdp.pc.robot.pilot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import sdp.pc.common.ChooseRobot;
import sdp.pc.vision.Point2;
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
public class Strategy implements Runnable {

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
	 * Controls how often to parse/send commands to the robots. 1/7*1000 = 7
	 * times per second. Never use integer values to describe double precision
	 * numbers. 7 != 7.0
	 */
	private static final double PERIOD = 1.0 / 7.0 * 1000.0;

	/**
	 * The minimum speed for the ball to be considered fast, in pixels per
	 * second.
	 */
	private static final double FAST_BALL_SPEED = 150.0;

	/**
	 * How much to contract the size of the goal when looking for a random point
	 */
	private static final int GOAL_CONTRACT_SIZE = 5;

	/**
	 * A counter used to print statements to the screen without flooding the
	 * console.
	 */
	private static int frameCounter = 0;

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

	private static Strategy instance;

	private boolean interrupted = false;

	private static Point2 targetPoint = new Point2();

	private static Point2 lastBallPos = new Point2();

	/**
	 * Builds the vision system
	 * 
	 * @throws InterruptedException
	 */
	public void startVisionSystem() throws InterruptedException {
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
	 * Turns a quadrant into the correct description of the ball as a String.
	 * Possible results include:
	 * 
	 * <ul>
	 * <li>Enemy Defender</li>
	 * <li>Our Attacker</li>
	 * <li>Enemy Attacker</li>
	 * <li>Our Defender</li>
	 * <li>"" Empty String (unrecognised input)</li>
	 * </ul>
	 * 
	 * This method assumes state.getDirection() works as intended.
	 * 
	 * @param quad
	 *            - the integer quadrant the ball is in (0, 1, 2, 3, or 4)
	 * @return - String describing ball position
	 */
	private String parseQuadrant(int quad) {
		if (state.getDirection() == 0) {
			if (quad == 1) {
				return "Enemy Defender";
			} else if (quad == 2) {
				return "Our Attacker";
			} else if (quad == 3) {
				return "Enemy Attacker";
			} else if (quad == 4) {
				return "Our Defender";
			}
		} else {
			if (quad == 1) {
				return "Our Defender";
			} else if (quad == 2) {
				return "Enemy Attacker";
			} else if (quad == 3) {
				return "Our Attacker";
			} else if (quad == 4) {
				return "Enemy Defender";
			}
		}
		return "";
	}

	/**
	 * The robots should follow a state-based pattern depending on the ball's
	 * location and velocity. Use this to update the states automatically.
	 * 
	 * <table>
	 * <tr>
	 * <td>Ball Position</td>
	 * <td>Defender</td>
	 * <td>Attacker</td>
	 * </tr>
	 * <tr>
	 * <td>Enemy Attacker</td>
	 * <td>Fast Ball ? Defend Ball : Defend Enemy Attacker</td>
	 * <td>Get Ready to receive pass</td>
	 * </tr>
	 * <tr>
	 * <td>Enemy Defender</td>
	 * <td>Fast Ball ? Defend Ball : Defend Goal Line</td>
	 * <td>Fast Ball ? Intercept Ball : Defend Enemy Defender</td>
	 * </tr>
	 * <tr>
	 * <td>Our Attacker</td>
	 * <td>Fast Ball ? Defend Ball : Defend Goal Line</td>
	 * <td>Fast Ball ? Intercept Ball : Get Ball</td>
	 * </tr>
	 * <tr>
	 * <td>Our Defender</td>
	 * <td>Fast Ball ? Defend Ball : Pass to Attacker</td>
	 * <td>Fast Ball ? Intercept Ball : Prepare for Pass</td>
	 * </tr>
	 * </table>
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void updateStates() throws Exception {

		// Calculate Ball Position
		int quad = state.getBallQuadrant();
		boolean aQ = attacker.nearBoundary();
		boolean dQ = defender.nearBoundary();
		String position = parseQuadrant(quad);

		// Calculate relative velocities
		Point2 ballPos = state.getBallPosition();
		Point2 vel = state.getBallVelocity();
		double speedWrtAttacker = getSpeedWrt(ballPos, vel, attacker);
		double speedWrtDefender = getSpeedWrt(ballPos, vel, defender);

		// Since getSpeedWrt is unimplemented, just use the literal ball speed:
		double speed = vel.modulus();

		// Set states
		if (position.equals("Enemy Attacker")) {
			attacker.setState(Robot.State.WAIT_RECEIVE_PASS);
			if (speed > FAST_BALL_SPEED) {
				defender.setState(Robot.State.DEFEND_BALL);
			} else {
				defender.setState(Robot.State.DEFEND_ENEMY_ATTACKER);
			}
		} else if (position.equals("Enemy Defender")) {
			if (speed > FAST_BALL_SPEED) {
				defender.setState(Robot.State.DEFEND_BALL);
				attacker.setState(Robot.State.DEFEND_BALL);
			} else {
				defender.setState(Robot.State.DEFEND_GOAL_LINE);
				attacker.setState(Robot.State.DEFEND_ENEMY_DEFENDER);
			}
		} else if (position.equals("Our Attacker")) {
			if (speed > FAST_BALL_SPEED && attacker.getSubState() == 0) {
				defender.setState(Robot.State.DEFEND_BALL);
				attacker.setState(Robot.State.DEFEND_BALL);
			} else {
				defender.setState(Robot.State.DEFEND_BALL);
				if (attacker.getKickSubState() >= 5) {
					attacker.setState(Robot.State.ATTEMPT_GOAL);
				} else {
					if (attacker.getState() != Robot.State.GET_BALL) {
						targetPoint = getTheirGoalRandom();
					}
					attacker.setState(Robot.State.GET_BALL);
				}
			}
		} else if (position.equals("Our Defender")) {
			if (speed > FAST_BALL_SPEED && defender.getSubState() == 0) {
				defender.setState(Robot.State.DEFEND_BALL);
				attacker.setState(Robot.State.DEFEND_BALL);
			} else {
				defender.setState(Robot.State.PASS_TO_ATTACKER);
				attacker.setState(Robot.State.WAIT_RECEIVE_PASS);
			}
		} else {
			// We have a state for unknown robot positions, which means unknown
			// state can just persist the previous state
			state.setBallPosition(lastBallPos);
		}

		// Reset states to "nothing" if the robot positions are unknown
		if (state.getRobotPosition(defender.getTeam(), defender.getId())
				.equals(Point2.EMPTY))
			defender.setState(Robot.State.DO_NOTHING);
		if (state.getRobotPosition(attacker.getTeam(), attacker.getId())
				.equals(Point2.EMPTY))
			attacker.setState(Robot.State.DO_NOTHING);

		// Override all states to "reset" if the robot gets close to its
		// boundary
		if (attacker.nearBoundary() && attacker.getKickSubState() >= 0) {
			attacker.setState(Robot.State.RESET);
			System.err.println("Resetting attacker");
		}
		if (defender.nearBoundary() && defender.getKickSubState() >= 0) {
			defender.setState(Robot.State.RESET);
			System.err.println("Resetting defender");
		}
		lastBallPos = state.getBallPosition();
	}

	/**
	 * The speed of the ball with respect to the robot is just the ball's
	 * velocity vector dotted to the vector between the ball and the robot,
	 * divided by the modulus of the vector between ball and robot. See
	 * http://goo.gl/mJdekQ for example
	 * 
	 * @param ballPos
	 *            - position of the ball
	 * @param ballVel
	 *            - velocity of the ball
	 * @param bot
	 *            - a Robot object
	 * @return - the speed of the ball with respect to the robot (how fast the
	 *         ball is moving towards the the robot
	 */
	private double getSpeedWrt(Point2 ballPos, Point2 ballVel, Robot bot) {

		// Initialise necessary components for calculation
		Point2 robotPosition = state.getRobotPosition(bot.getTeam(),
				bot.getId());
		Point2 ballToBot = robotPosition.sub(ballPos);
		double dist = ballToBot.modulus();

		// Calculate the projection
		return (ballVel.dot(ballToBot) / dist);
	}

	/**
	 * Main logic branching mechanism for attacker. Use attacker.getState() as
	 * well.
	 * <p />
	 * States an attacker can be in:
	 * <ul>
	 * <li>Robot.State.WAIT_RECEIVE_PASS</li>
	 * <li>Robot.State.DEFEND_BALL</li>
	 * <li>Robot.State.DEFEND_ENEMY_DEFENDER</li>
	 * <li>Robot.State.GET_BALL</li>
	 * </ul>
	 * 
	 * @throws Exception
	 */
	private void parseAttacker() throws Exception {
		int botState = attacker.getState();
		if (botState == Robot.State.DO_NOTHING) {
			attacker.stop();
		} else if (botState == Robot.State.WAIT_RECEIVE_PASS) {
			attacker.defendRobot(attacker.getTeam(), attacker.getOtherId());
		} else if (botState == Robot.State.DEFEND_BALL) {
			attacker.defendBall();
		} else if (botState == Robot.State.DEFEND_ENEMY_DEFENDER) {
			attacker.defendRobot(attacker.getOtherTeam(), attacker.getId());
		} else if (botState == Robot.State.GET_BALL) {
			attacker.kickBallToPoint(targetPoint);
		} else if (botState == Robot.State.ATTEMPT_GOAL) {
			executeShootStrategy();
		} else if (botState == Robot.State.RESET) {

			// TODO: Just go there the best way
			attacker.goToReverse(
					state.getPitch()
							.getQuadrantCenter(attacker.getMyQuadrant()), 10.0);
		} else {
			attacker.assertPerpendicular(10.0);
		}
	}

	/**
	 * Main logic branching mechanism for defender. Use defender.getState() as
	 * well.
	 * <p />
	 * States a defender can be in:
	 * <ul>
	 * <li>Robot.State.DEFEND_BALL</li>
	 * <li>Robot.State.DEFEND_ENEMY_ATTACKER</li>
	 * <li>Robot.State.DEFEND_GOAL_LINE</li>
	 * <li>Robot.State.PASS_TO_ATTACKER</li>
	 * </ul>
	 * 
	 * @throws Exception
	 */
	private void parseDefender() throws Exception {
		int botState = defender.getState();
		if (botState == Robot.State.DO_NOTHING) {
			defender.stop();
		} else if (botState == Robot.State.DEFEND_BALL) {
			defender.defendBall();
		} else if (botState == Robot.State.DEFEND_ENEMY_ATTACKER) {
			defender.defendRobot(defender.getOtherTeam(), defender.getId());
		} else if (botState == Robot.State.DEFEND_GOAL_LINE) {
			defender.defendWeightedGoalLine(0.5);
		} else if (botState == Robot.State.PASS_TO_ATTACKER) {
			defender.defenderPass();
		} else if (botState == Robot.State.RESET) {
			defender.goToReverse(
					state.getPitch()
							.getQuadrantCenter(defender.getMyQuadrant()), 10.0);
		} else {
			if (defender.assertNearGoalLine(10.0)) {
				if (defender.assertPerpendicular(10.0)) {
					defender.stop();
				}
			}
		}
	}

	/**
	 * Returns the random of the goal of their defender
	 * 
	 * @return
	 */
	private Point2 getTheirGoalRandom() {
		if (state.getDirection() == 0) {
			return state.getPitch().getLeftGoalRandom(GOAL_CONTRACT_SIZE);
		}
		return state.getPitch().getRightGoalRandom(GOAL_CONTRACT_SIZE);
	}

	/**
	 * Executes a shoot strategy. Could have conditions on which strategy to use
	 * in the future.
	 * 
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private void executeShootStrategy() throws InterruptedException,
			Exception {
		// If the enemy defender has been removed from the pitch and we are in a
		// position to score
		// then aim shoot at the centre of the correct goal. Otherwise perform
		// our normal shoot strategy.
		if (state.getRobotPosition(attacker.getId(), (1 - attacker.getTeam())) == Point2.EMPTY
				&& attacker.onShootPoint) {
			if (state.getDirection() == 1) {
				attacker.kickBallToPoint(state.getRightGoalCentre());
			} else {
				attacker.kickBallToPoint(state.getLeftGoalCentre());
			}
		} else {
			attacker.shootStrategy1();
		}
	}

	/**
	 * Loops indefinitely, ordering the robots to do things
	 * 
	 * @throws Exception
	 */
	private void executeStrategy() throws Exception {

		Thread.sleep(1000);
		attacker.getDriver().open();
		defender.getDriver().open();
		Thread.sleep(500);
		while (true) {
			try {
				if (interrupted) {
					attacker.stop();
					defender.stop();
					return;
				}

				updateStates();

				printStatesPeriodically();

				parseAttacker();

				parseDefender();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
			final Strategy s = new Strategy();
			s.startVisionSystem();

			// Add a start button so we can have time to calibrate etc
			JFrame frame = new JFrame("Ready and waiting!");
			ImageIcon q = new ImageIcon("resource/strategy.png");
			frame.setIconImage(q.getImage());
			frame.setBounds(600, 200, 300, 150);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			final JButton button = new JButton();
			button.setText("Connect");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if (guiState == 0) {
							// Set our team and direction
							myTeam = s.getState().getOurColor();
							attackerId = s.getState().getDirection();
							defenderId = 1 - s.getState().getDirection();
							System.out
									.println("Sanity Check! " + "Our Team: "
											+ myTeam + ", Defender Side: "
											+ defenderId);
							// Connect to robots
							s.connectRobots();
							addShutdownHook();

							// Change the button
							button.setText("Start");

							guiState = 1;
						} else if (guiState == 1) {
							// Begin looping through the strategy functionality
							// executeStrategy();
							instance = new Strategy();
							instance.interrupted = false;
							(new Thread(instance)).start();
							button.setText("Pause");
							guiState = 2;
						} else if (guiState == 2) {
							instance.interrupted = true;
							button.setText("Restart");
							guiState = 1;
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			frame.add(button);
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			executeStrategy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void printStatesPeriodically() {

		// Print out the states every 10 frames (don't flood the console)
		frameCounter++;
		if (frameCounter == 10) {
			System.out.println();
			System.out.println("Attacker, Defender states:");
			System.out.println("Attacker is turning to the top corner:"
					+ !attacker.turnedTowardsTopOfGoal);
			System.out.println("Attacker is holding the ball:"
					+ (attacker.getKickSubState() >= 6));
			System.out.println("Attacker, Defender states:");
			Robot.State.print(attacker.getState());
			Robot.State.print(defender.getState());
			frameCounter = 0;
		}
	}
	
	public WorldState getState(){
		return this.state;
	}
}
