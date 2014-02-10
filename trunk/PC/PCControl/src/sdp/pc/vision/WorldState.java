package sdp.pc.vision;

import sdp.pc.common.Constants;

/**
 * Class for recording and maintaining the positions and facing angles of
 * objects in "world". Must be instantiated.
 * 
 */
public class WorldState {
	public static final int PLAYERS_PER_TEAM = 2, TEAM_COUNT = 2;

	// Next three might need an enum, though we won't really need to ever touch
	// them here
	private int pitch; // 0 -> Main Pitch; 1 -> Side Pitch
	private int ourColor; // 0 -> Yellow; 1 -> Blue
	private int shootingDirection; // 0 -> Left; 1 -> Right

	private Point2 ballLocation = new Point2();
	private Point2 ballVelocity = new Point2();
	private double ballFacing, ballSpeed;
	private Point2[][] robotLoc = new Point2[TEAM_COUNT][PLAYERS_PER_TEAM];
	private double[][] robotFacing = new double[TEAM_COUNT][PLAYERS_PER_TEAM];

	// Those are assigned on start either from default value or config file
	public static Point2 leftGoalTop;
	public static Point2 leftGoalBottom;
	public static Point2 rightGoalTop;
	public static Point2 rightGoalBottom;
	public static Point2 leftGoalCentre;
	public static Point2 rightGoalCentre;

	public WorldState() {

		// Initialise robot locations to null values
		for (int t = 0; t < TEAM_COUNT; t++)
			for (int p = 0; p < PLAYERS_PER_TEAM; p++)
				robotLoc[t][p] = new Point2();
	}

	public static void resetGoal() {
		leftGoalTop = null;
		leftGoalBottom = null;
		rightGoalTop = null;
		rightGoalBottom = null;
		leftGoalCentre = null;
		rightGoalCentre = null;
	}

	public double getBallFacing() {
		return ballFacing;
	}

	public double getBallSpeed() {
		return ballSpeed;
	}

	// const getters
	public Point2 getLeftGoalCentre() {
		return Constants.LEFT_GOAL_CENTRE;
	}

	public Point2 getRightGoalCentre() {
		return Constants.RIGHT_GOAL_CENTRE;
	}

	/**
	 * Gets the current position of the ball
	 */
	public Point2 getBallPosition() {
		return ballLocation;
	}

	/**
	 * Sets the current position of the ball
	 */
	public void setBallPosition(Point2 newPos) {
		this.ballLocation = newPos;
	}

	/**
	 * Gets the position of the specified robot
	 * 
	 * @param team
	 *            the team of the robot, 0 for yellow and 1 for blue
	 * @param robot
	 *            the id of the robot, 0 for left one and 1 foe the right one
	 * @return the position of the robot
	 */
	public Point2 getRobotPosition(int team, int robot) {
		return robotLoc[team][robot];
	}

	/***
	 * Updates the team position of a given robot, part of a given team.
	 * 
	 * @param team
	 *            - the team of the updated robot.
	 * @param robot
	 *            - the robot to update.
	 * @param newLoc
	 *            - the updated position of the robot.
	 */
	public void setRobotPosition(int team, int robot, Point2 newLoc) {
		this.robotLoc[team][robot] = newLoc;
	}

	/**
	 * Gets the facing of the specified robot
	 * 
	 * @param team
	 *            the team of the robot, 0 for yellow and 1 for blue
	 * @param robot
	 *            the id of the robot, 0 for left one and 1 for the right one
	 * @return the orientation of the robot, in ???
	 */
	public double getRobotFacing(int team, int robot) {
		return robotFacing[team][robot];
	}

	/**
	 * Updates the facing of a given robot, part of a given team.
	 * 
	 * @param team
	 *            - the team of the updated robot.
	 * @param robot
	 *            - the robot to update.
	 * @param newFacing
	 *            - the updated angle of facing for the robot.
	 */
	public void setRobotFacing(int team, int robot, double newFacing) {
		this.robotFacing[team][robot] = newFacing;
	}

	/**
	 * Set which pitch we're playing on (0 is Main, 1 is Side)
	 */
	public void setPitch(int pitch) {
		this.pitch = pitch;
	}

	/**
	 * Get which pitch we're playing on (0 is Main, 1 is Side)
	 */
	public int getPitch() {
		return pitch;
	}

	/**
	 * Set our team's colour (0 is yellow, 1 is blue)
	 */
	public void setOurColor(int ourColor) {
		this.ourColor = ourColor;
	}

	/**
	 * Get our team's colour (0 is yellow, 1 is blue)
	 */
	public int getOurColor() {
		return ourColor;
	}

	/**
	 * Gets the direction our team is supposed to shoot towards (0 is left)
	 */
	public int getDirection() {
		return shootingDirection;
	}

	/**
	 * Sets the direction our team is supposed to shoot towards (0 is left)
	 */
	public void setDirection(int direction) {
		this.shootingDirection = direction;
	}

	/**
	 * Sets the current velocity of the ball
	 */

	public void setBallVelocity(Point2 ballVelocity) {
		this.ballVelocity = ballVelocity;
		this.ballSpeed = ballVelocity.length();
		this.ballFacing = ballVelocity.angle();
	}

	/**
	 * Gets the current velocity of the ball
	 */

	public Point2 getBallVelocity() {
		return ballVelocity;
	}

}