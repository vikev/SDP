package sdp.pc.robot.pilot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import sdp.pc.common.ChooseRobot;
import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;

/**
 * Strategy is the global static class for running a match in SDP. Here we
 * initialise a Vision and Robot pair, and conduct all the state-based decision
 * model related data to command the robot pair. Then we iterate several times
 * per second, parsing the game state and ordering the robots to do things.
 * 
 * @author s1133141
 * 
 */
public class Strategy {

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
	private static final double PERIOD = 1.0 / 5.0 * 1000.0;

	/**
	 * The minimum speed for the ball to be considered fast, in pixels per
	 * second.
	 */
	private static final double FAST_BALL_SPEED = 150.0;

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

	@SuppressWarnings("unused")
	private static Point2 basicGoalTarget() {
		if (state.getDirection() == 0) {
			if (state.getRobotPosition(myTeam ^ 1, defenderId).getY() > state
					.getLeftGoalCentre().getY())
				return state.leftGoalTop;
			else
				return state.leftGoalBottom;
		} else {
			if (state.getRobotPosition(myTeam ^ 1, defenderId).getY() > state
					.getRightGoalCentre().getY())
				return state.rightGoalTop;
			else
				return state.rightGoalBottom;
		}
	}

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
	private static String parseQuadrant(int quad) {
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
	 */
	@SuppressWarnings("unused")
	private static void updateStates() {

		// Calculate Ball Position
		int quad = state.getBallQuadrant();
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
			if (speed > FAST_BALL_SPEED) {
				defender.setState(Robot.State.DEFEND_BALL);
				attacker.setState(Robot.State.DEFEND_BALL);
			} else {
				defender.setState(Robot.State.DEFEND_BALL);
				attacker.setState(Robot.State.GET_BALL);
			}
		} else if (position.equals("Our Defender")) {
			if (speed > FAST_BALL_SPEED) {
				defender.setState(Robot.State.DEFEND_BALL);
				attacker.setState(Robot.State.DEFEND_BALL);
			} else {
				defender.setState(Robot.State.PASS_TO_ATTACKER);
				attacker.setState(Robot.State.WAIT_RECEIVE_PASS);
			}
		} else {
			// We have a state for unknown robot positions, which means unknown
			// state can just persist the previous state
		}

		// Reset states to "nothing" if the robot positions are unknown
		if (state.getRobotPosition(defender.getTeam(), defender.getId())
				.equals(Point2.EMPTY))
			defender.setState(Robot.State.DO_NOTHING);
		if (state.getRobotPosition(attacker.getTeam(), attacker.getId())
				.equals(Point2.EMPTY))
			attacker.setState(Robot.State.DO_NOTHING);

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
	private static double getSpeedWrt(Point2 ballPos, Point2 ballVel, Robot bot) {

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
	private static void parseAttacker() throws Exception {
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
			attacker.kickBallToPoint(getTheirGoalCentre());
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
	private static void parseDefender() throws Exception {
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
		} else {
			if (defender.assertNearGoalLine(10.0)) {
				if (defender.assertPerpendicular(10.0)) {
					defender.stop();
				}
			}
		}
	}

	/**
	 * Returns the centre of the goal of their defender
	 * 
	 * @return
	 */
	private static Point2 getTheirGoalCentre() {
		if (state.getDirection() == 0) {
			return state.getLeftGoalCentre();
		}
		return state.getRightGoalCentre();
	}

	// temporary frame counter (trafendersh)
	private static int q = 0;

	/**
	 * Loops indefinitely, ordering the robots to do things
	 * 
	 * @throws InterruptedException
	 */
	private static void executeStrategy() throws InterruptedException {
		Thread.sleep(1000);
		while (true) {
			try {
				updateStates();

				// Print out the states every 10 frames (don't flood the
				// console)
				q++;
				if (q == 10) {
					System.out.println();
					System.out.println("Attacker, Defender states:");
					Robot.State.print(attacker.getState());
					Robot.State.print(defender.getState());
					q = 0;
				}

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
			startVisionSystem();

			// Add a start button so we can have time to calibrate etc
			JFrame frame = new JFrame("Ready and waiting!");
			frame.setBounds(600, 200, 300, 150);
			final JButton button = new JButton();
			button.setText("Connect");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if (button.getText().equals("Connect")) {
							// Set our team and direction
							myTeam = state.getOurColor();
							attackerId = state.getDirection();
							defenderId = 1 - state.getDirection();
							System.out
									.println("Sanity Check! " + "Our Team: "
											+ myTeam + ", Defender Side: "
											+ defenderId);
							// Connect to robots
							connectRobots();
							addShutdownHook();

							// Change the button
							button.setText("Start");
						} else {
							// Begin looping through the strategy functionality
							executeStrategy();
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
}
