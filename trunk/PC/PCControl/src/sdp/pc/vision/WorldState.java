package sdp.pc.vision;

import sdp.pc.common.Constants;

/**
 * Class for recording and maintaining the positions and facing angles of
 * objects in "world". Must be instantiated.
 * 
 */
public class WorldState {

	/**
	 * Simple value for the number of players per team. TODO: Should probably be
	 * moved to a global constants class to avoid code duplication.
	 */
	public static final int PLAYERS_PER_TEAM = 2;

	/**
	 * Simple value for the number of teams. TODO: Should probably be moved to a
	 * global constants class to avoid code duplication.
	 */
	public static final int TEAM_COUNT = 2;

	/**
	 * The pitch associated with this instance of WorldState. 0 for Main, 1 for
	 * side (TODO: Should be abstracted)
	 */
	private int pitch;

	/**
	 * The colour of our team in this instance of WorldState. 0 for Yellow, 1
	 * for Blue. (TODO: Should be abstracted)
	 */
	private int ourColor;

	/**
	 * Which direction our robots are aiming for in this instance of WorldState.
	 * 0 for left, 1 for right. (TODO: Should be abstracted)
	 */
	private int shootingDirection;

	/**
	 * The best known location of the ball in <b>this</b>. Is reset to
	 * Point2.EMPTY when position is unknown.
	 */
	private Point2 ballLocation = new Point2();

	/**
	 * The best known velocity of the ball in <b>this</b>. It is reset to
	 * Point2.EMPTY when unknown. Note that Point2 is used because x,y refer to
	 * the del properties of the ball (Difference in x and y of the ball from
	 * frame to frame)
	 */
	private Point2 ballVelocity = new Point2();

	/**
	 * A point given by futureball as the estimated point the ball will stop,
	 * given its current velocity and position
	 */
	private Point2 estimatedStopLocation = new Point2();

	/**
	 * A point given by futureball as the estimated point the ball will collide
	 * with a wall (or goal), using the current ball velocity and location data
	 */
	private Point2 estimatedCollidePoint = new Point2();

	/**
	 * The movement angle of the ball in degrees
	 */
	private double ballFacing;

	/**
	 * The movement speed of the ball TODO: Units? Should likely use pixels per
	 * second.
	 */
	private double ballSpeed;

	/**
	 * Set of robot locations (Reset to Point2.EMPTY if a hat is not found)
	 */
	private Point2[][] robotLoc = new Point2[TEAM_COUNT][PLAYERS_PER_TEAM];

	/**
	 * Set of robot facing angles TODO: Units? should be degrees on [0,360).
	 * What is it set to if the robot position is unknown?
	 */
	private double[][] robotFacing = new double[TEAM_COUNT][PLAYERS_PER_TEAM];

	/**
	 * Goal points established using calibration values in the settings GUI.
	 */
	public static Point2 leftGoalTop = new Point2();

	/**
	 * Goal points established using calibration values in the settings GUI.
	 */
	public static Point2 leftGoalBottom = new Point2();

	/**
	 * Goal points established using calibration values in the settings GUI.
	 */
	public static Point2 rightGoalTop = new Point2();

	/**
	 * Goal points established using calibration values in the settings GUI.
	 */
	public static Point2 rightGoalBottom = new Point2();

	/**
	 * Goal points calculated using calibration values from the settings GUI.
	 */
	public static Point2 leftGoalCentre = new Point2();

	/**
	 * Goal points calculated using calibration values from the settings GUI.
	 */
	public static Point2 rightGoalCentre = new Point2();

	/**
	 * Simple constructor which sets robot positions to empty.
	 */
	public WorldState() {
		for (int t = 0; t < TEAM_COUNT; t++)
			for (int p = 0; p < PLAYERS_PER_TEAM; p++)
				robotLoc[t][p] = new Point2();
	}

	/**
	 * Reset method for goal points.
	 */
	public static void resetGoal() {
		leftGoalTop = null;
		leftGoalBottom = null;
		rightGoalTop = null;
		rightGoalBottom = null;
		leftGoalCentre = null;
		rightGoalCentre = null;
	}

	/**
	 * Getter method for ball facing
	 * 
	 * @return TODO: Returns ball facing in degrees on [0,360)? (It should)
	 */
	public double getBallFacing() {
		return ballFacing;
	}

	/**
	 * Getter method for ball speed
	 * 
	 * @return TODO: Ball speed in pixels per second? Or per frame? To discuss.
	 */
	public double getBallSpeed() {
		return ballSpeed;
	}

	/**
	 * Getter method for left goal centre
	 * 
	 * @return Left Goal Centre as a Point2
	 */
	public Point2 getLeftGoalCentre() {
		if (leftGoalCentre == null) {
			leftGoalCentre = new Point2(
					(int) (leftGoalTop.getX() + leftGoalBottom.getX()) / 2,
					(int) (leftGoalTop.getY() + leftGoalBottom.getY()) / 2);
		}
		return leftGoalCentre;

	}

	/**
	 * Getter method for right goal centre
	 * 
	 * @return Right Goal Centre as a Point2
	 */
	public Point2 getRightGoalCentre() {
		if (rightGoalCentre == null) {
			rightGoalCentre = new Point2(
					(int) (rightGoalTop.getX() + rightGoalBottom.getX()) / 2,
					(int) (rightGoalTop.getY() + rightGoalBottom.getY()) / 2);
		}
		return rightGoalCentre;
	}

	/**
	 * Getter method for ball position
	 * 
	 * @return ball position as a Point2
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
	 *            the team of the robot, 0 for yellow and 1 for blue TODO:
	 *            Should be abstracted
	 * @param robot
	 *            the id of the robot, 0 for left one and 1 for the right one
	 *            TODO: Should be abstracted
	 * @return the position of the requested robot as a Point2
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
	 * @return the orientation of the robot, in TODO:
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
	 *            - the updated angle of facing for the robot. TODO: Units
	 */
	public void setRobotFacing(int team, int robot, double newFacing) {
		this.robotFacing[team][robot] = newFacing;
	}

	/**
	 * Set which pitch we're playing on (0 is Main, 1 is Side) TODO: Should be
	 * abstracted
	 */
	public void setPitch(int pitch) {
		this.pitch = pitch;
	}

	/**
	 * Get which pitch we're playing on (0 is Main, 1 is Side) TODO: Should be
	 * abstracted
	 */
	public int getPitch() {
		return pitch;
	}

	/**
	 * Set our team's colour (0 is yellow, 1 is blue) TODO: Should be abstracted
	 */
	public void setOurColor(int ourColor) {
		this.ourColor = ourColor;
	}

	/**
	 * Get our team's colour (0 is yellow, 1 is blue) TODO: Should be abstracted
	 */
	public int getOurColor() {
		return ourColor;
	}

	/**
	 * Gets the direction our team is supposed to shoot towards (0 is left)
	 * TODO: Should be abstracted
	 */
	public int getDirection() {
		return shootingDirection;
	}

	/**
	 * Sets the direction our team is supposed to shoot towards (0 is left)
	 * TODO: Should be abstracted
	 */
	public void setDirection(int direction) {
		this.shootingDirection = direction;
	}

	/**
	 * Sets the current velocity of the ball TODO: Units
	 */

	public void setBallVelocity(Point2 ballVelocity) {
		this.ballVelocity = ballVelocity;
		this.ballSpeed = ballVelocity.length();
		this.ballFacing = ballVelocity.angle();
	}

	/**
	 * Gets the current velocity of the ball TODO: Units
	 */

	public Point2 getBallVelocity() {
		return ballVelocity;
	}

	/**
<<<<<<< HEAD
	 * Setter method for updating the estimated ball stop point
=======
	 * Setter method for updating the estimated ball stop point. Used by
	 * FutureBall
>>>>>>> b54798607711c526d0c9a49fa2efc4fbff123854
	 * 
	 * @param pt
	 */
	public void setEstimatedStopPoint(Point2 pt) {
		this.estimatedStopLocation = pt;
	}

	/**
<<<<<<< HEAD
	 * Setter method for updating the estimated collide point
=======
	 * Setter method for updating the estimated collide point. Used by
	 * FutureBall
>>>>>>> b54798607711c526d0c9a49fa2efc4fbff123854
	 * 
	 * @param pt
	 */
	public void setEstimatedCollisionPoint(Point2 pt) {
		this.estimatedCollidePoint = pt;
	}

	/**
<<<<<<< HEAD
	 * Getter method for the estimated stop point of the ball
=======
	 * Getter method for the estimated stop point of the ball. Used by
	 * FutureBall
>>>>>>> b54798607711c526d0c9a49fa2efc4fbff123854
	 * 
	 * @return
	 */
	public Point2 getEstimatedStopPoint() {
		return this.estimatedStopLocation;
	}

	/**
<<<<<<< HEAD
	 * Getter method for the estimated collide point of the ball
=======
	 * Getter method for the estimated collide point of the ball. Used by
	 * FutureBall
>>>>>>> b54798607711c526d0c9a49fa2efc4fbff123854
	 * 
	 * @return
	 */
	public Point2 getEstimatedCollidePoint() {
		return this.estimatedCollidePoint;
	}
}
