package sdp.pc.robot.pilot;

import sdp.pc.vision.Point2;
import sdp.pc.vision.WorldState;

/**
 * StrategyThread is where we actually order the robots to do things. The GUI in
 * Strategy launches (and interrupts) instances of StrategyThread.
 * 
 * @author s1143704
 * 
 */
public class StrategyThread extends Thread {

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
	private static final double FAST_BALL_SPEED = 100.0;

	private static Point2 targetPoint = new Point2();

	private static Point2 lastBallPos = new Point2();

	/**
	 * A counter used to print statements to the screen without flooding the
	 * console.
	 */
	private static int frameCounter = 0;

	private Robot attacker;
	private Robot defender;
	private WorldState state;
	private boolean interrupted = false;

	public StrategyThread(Robot attacker, Robot defender, WorldState state) {
		this.attacker = attacker;
		this.defender = defender;
		this.state = state;
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
		String position = state.parseQuadrant(quad);

		// Calculate relative velocities
		Point2 ballPos = state.getBallPosition();
		Point2 vel = state.getBallVelocity();
		double speedWrtAttacker = state.getSpeedWrt(ballPos, vel, attacker);
		double speedWrtDefender = state.getSpeedWrt(ballPos, vel, defender);

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
						targetPoint = state.getTheirGoalRandom();
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
				System.out.println(state.getBallVelocity().modulus());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep((int) PERIOD);
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
			if (defender.getPrevState() == Robot.State.PASS_TO_ATTACKER) {
				defender.turnTo(state.getBallPosition(), 20.0);
			} else {
				defender.goToReverse(
						state.getPitch()
								.getQuadrantCenter(defender.getMyQuadrant()), 10.0);
			}
		} else {
			if (defender.assertNearGoalLine(10.0)) {
				if (defender.assertPerpendicular(10.0)) {
					defender.stop();
				}
			}
		}
	}

	private void printStatesPeriodically() {

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

			// TODO: Verify doesn't cause any problems.
			Point2 target = state.getPitch().getQuadrantCenter(
					attacker.getMyQuadrant());
			attacker.goToFast(target, 10.0);
		} else {
			attacker.assertPerpendicular(10.0);
		}
	}

	/**
	 * Executes a shoot strategy. Could have conditions on which strategy to use
	 * in the future.
	 * 
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private void executeShootStrategy() throws InterruptedException, Exception {
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

	@Override
	public void run() {
		try {
			executeStrategy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void interrupt() {
		this.interrupted = true;
	}
}
