package sdp.pc.vision;

import sdp.pc.common.Constants;

/**
 * Class for recording and maintaining the positions and facing angles of
 * objects in "world". Must be instantiated.
 * 
 */
public class WorldState {
	public static final int PLAYERS_PER_TEAM = 2, TEAM_COUNT = 2,
			TEAM_YELLOW = 0, TEAM_BLUE = 1;

	public int targetGoal = Constants.GOAL_LEFT;
	private int direction;
	private Point2 ballLocation;
	private double ballFacing;
	private Point2[][] robotLoc = new Point2[TEAM_COUNT][PLAYERS_PER_TEAM];
	private double[][] robotFacing = new double[TEAM_COUNT][PLAYERS_PER_TEAM];

	// Taken from image of pitch; these are likely to change
	public static final Point2 leftGoalCentre = new Point2(77, 235);
	public static final Point2 rightGoalCentre = new Point2(589, 241);

	public WorldState() {

		// Set initial direction
		if (targetGoal == Constants.GOAL_LEFT) {
			this.direction = Constants.DIRECTION_LEFT;
		} else {
			this.direction = Constants.DIRECTION_RIGHT;
		}

		// Initialise robot locations to null values
		for (int t = 0; t < TEAM_COUNT; t++)
			for (int p = 0; p < PLAYERS_PER_TEAM; p++)
				robotLoc[t][p] = new Point2();
	}

	// Getter Methods
	public Point2 getBallPosition() {
		return ballLocation;
	}
	
	public double getBallFacing() {
		return ballFacing;
	}

	public int getDirection() {
		return direction;
	}

	public int getTargetGoal() {
		return targetGoal;
	}

	public Point2 getLeftGoalCentre() {
		return Constants.LEFT_GOAL_CENTRE;
	}

	public Point2 getRightGoalCentre() {
		return Constants.RIGHT_GOAL_CENTRE;
	}

	/**
	 * Method for choosing a new target goal
	 * 
	 * @param newTargetGoal
	 */
	public void setTargetGoal(int newTargetGoal) {
		targetGoal = newTargetGoal;
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

	/***
	 * Updates the position of the ball.
	 * 
	 * @param newPos
	 *            - the new position of the ball.
	 */
	public void setBallPosition(Point2 newPos) {
		this.ballLocation = newPos;
	}
	
	/***
	 * Updates the facing of the ball.
	 * 
	 * @param newFacing
	 *            - the new facing of the ball in degrees and -1 if ball is not moving.
	 */
	public void setBallFacing(double newFacing) {
		this.ballFacing = newFacing;
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
	 * Set the direction our team is supposed to shoot towards.
	 * 
	 * @param direction
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

}