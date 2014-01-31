package sdp.pc.vision;

import sdp.pc.common.Constants;

public class WorldState {
	public static final int PLAYERS_PER_TEAM = 2, TEAM_COUNT = 2,
			TEAM_YELLOW = 0, TEAM_BLUE = 1;

	public static final Point2 leftGoalCentre = new Point2(77, 235);
	public static final Point2 rightGoalCentre = new Point2(589, 241);
	public int targetGoal = 1; // 1 is left goal 0 is right goal

	private int direction; // 0 = right, 1 = left.
	private Point2 ball;
	private Point2[][] robotLoc = new Point2[TEAM_COUNT][PLAYERS_PER_TEAM];
	private double[][] robotFacing = new double[TEAM_COUNT][PLAYERS_PER_TEAM];

	public WorldState() {

		// Control properties
		this.direction = 0;

		// Object properties
		for (int t = 0; t < TEAM_COUNT; t++)
			for (int p = 0; p < PLAYERS_PER_TEAM; p++)
				robotLoc[t][p] = new Point2();
	}

	/* Getter Methods */
	public Point2 getBallPosition() {
		return ball;
	}

	public int getDirection() {
		return direction;
	}

	public int getTargetGoal() {
		return targetGoal;
	}

	public void setTargetGoal(int newTargetGoal) {
		targetGoal = newTargetGoal;
	}

	public Point2 getLeftGoalCentre() {
		return leftGoalCentre;
	}

	public Point2 getRightGoalCentre() {
		return rightGoalCentre;
	}

	public Point2 getRobotPosition(int team, int robot) {
		return robotLoc[team][robot];
	}

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
		this.ball = newPos;
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