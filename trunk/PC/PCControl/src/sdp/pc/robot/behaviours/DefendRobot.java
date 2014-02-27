package sdp.pc.robot.behaviours;

import sdp.pc.robot.pilot.Robot;

/**
 * Behaviour which performs the goal of defending the robot. It
 * should only be called when the ball is not moving (or ball is not on the
 * pitch) and the opponent's attacker is on the pitch. It checks the
 * predicted stop location of the imaginary ball if the attacking robot
 * would kick now and moves to predicted position's Y coordinate by going
 * forwards or backwards.
 * TODO Currently works only for defender
 */
public class DefendRobot extends MyBehavior {

	private static final double BALL_SPEED_THRESHOLD = 30.0;
	private static final double DEFEND_EPSILON_DISTANCE = 8.0;

	public DefendRobot(Robot robot) { super(robot); }

	/**
	 * Take control when the ball is not moving (or not on the pitch)
	 * and the opponent's attacker is on the pitch
	 */
	@Override
	public boolean takeControl() {
		//TODO
		return false;
		}

	@Override
	public void action() {
		while (!suppressed) {
			//TODO Implement this (by referring to Robot.java)
		}
	}
}