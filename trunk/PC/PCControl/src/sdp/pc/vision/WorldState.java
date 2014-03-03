package sdp.pc.vision;

import java.util.ArrayList;

/**
 * Class for recording and maintaining the positions and facing angles of
 * objects in "world". Must be instantiated.
 * 
 */
public class WorldState {

	/**
	 * Simple value for the number of players per team.
	 * 
	 * TODO: Should probably be moved to a global constants class to avoid code
	 * duplication.
	 */
	public static final int PLAYERS_PER_TEAM = 2;

	/**
	 * Simple value for the number of teams.
	 * 
	 * TODO: Should probably be moved to a global constants class to avoid code
	 * duplication.
	 */
	public static final int TEAM_COUNT = 2;

	/**
	 * The pitch ID associated with this instance of WorldState. 0 for Main, 1
	 * for side
	 * 
	 * (TODO: Should be abstracted)
	 */
	private int pitchId;

	/**
	 * The colour of our team in this instance of WorldState. 0 for Yellow, 1
	 * for Blue.
	 * 
	 * (TODO: Should be abstracted)
	 */
	private int ourColor;

	/**
	 * Which direction our robots are aiming for in this instance of WorldState.
	 * 0 for left, 1 for right.
	 * 
	 * (TODO: Should be abstracted)
	 */
	private int shootingDirection;

	/**
	 * The best known location of the ball in <b>this</b>. Is reset to
	 * Point2.EMPTY when position is unknown.
	 */
	private Point2 ballLocation = new Point2();

	/**
	 * The current pitch info for this world
	 */
	private final Pitch pitch = new Pitch();

	/**
	 * The framerate of the world state painter in frames per second. Useful for
	 * calculating the speed of things in meaningful units.
	 */
	private int paintFps;

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
	private Intersect futureData = new Intersect();

	/**
	 * The movement angle of the ball in degrees
	 */
	private double ballFacing;

	/**
	 * The movement speed of the ball in pixels per second
	 */
	private double ballSpeed;

	/**
	 * Set of robot locations (Reset to Point2.EMPTY if a hat is not found)
	 */
	private Point2[][] robotLoc = new Point2[TEAM_COUNT][PLAYERS_PER_TEAM];

	/**
	 * Robot facing angle in degrees on [0,360)
	 */
	private double[][] robotFacing = new double[TEAM_COUNT][PLAYERS_PER_TEAM];

	/**
	 * Goal points established using calibration values in the settings GUI.
	 */
	public Point2 leftGoalTop = new Point2();

	/**
	 * Goal points established using calibration values in the settings GUI.
	 */
	public Point2 leftGoalBottom = new Point2();

	/**
	 * Goal points established using calibration values in the settings GUI.
	 */
	public Point2 rightGoalTop = new Point2();

	/**
	 * Goal points established using calibration values in the settings GUI.
	 */
	public Point2 rightGoalBottom = new Point2();

	/**
	 * Goal points calculated using calibration values from the settings GUI.
	 */
	public Point2 leftGoalCentre = new Point2();

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
	public void resetGoal() {
		leftGoalTop = null;
		leftGoalBottom = null;
		rightGoalTop = null;
		rightGoalBottom = null;
		leftGoalCentre = null;
		rightGoalCentre = null;
	}

	/**
	 * Returns true if a given Y coordinate is between the specified goalmouth
	 * end points, with some epsilon value.
	 * 
	 * @param p
	 * @param side
	 * @param eps
	 * @return
	 */
	public boolean pointBetweenGoals(Point2 p, int side, int eps) {
		int y = p.getY();
		if (side == 0) {
			return (y + eps < leftGoalBottom.getY() && y - eps > leftGoalTop
					.getY());
		}
		return (y + eps < rightGoalBottom.getY() && y - eps > rightGoalTop
				.getY());
	}

	/**
	 * Getter method for paint FPS, useful for calculating the velocity of
	 * objects in meaningful units
	 */
	public int getPaintFps() {
		return this.paintFps;
	}

	/**
	 * Getter method for ball facing
	 * 
	 * @return the ball facing angle in degrees on [0,360)
	 */
	public double getBallFacing() {
		return ballFacing;
	}

	/**
	 * Getter method for ball speed.
	 * 
	 * @return Ball speed in pixels per second, as a double
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
		return pitch.getLeftGoalCentre();

	}

	/**
	 * Getter method for right goal centre
	 * 
	 * @return Right Goal Centre as a Point2
	 */
	public Point2 getRightGoalCentre() {
		return pitch.getRightGoalCentre();
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
	 *            the team of the robot, 0 for yellow and 1 for blue
	 * 
	 *            TODO: Should be abstracted
	 * @param robot
	 *            the id of the robot, 0 for left one and 1 for the right one
	 * 
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
	 * @return the orientation of the robot, in degrees on [0,360)
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
	 *            - the updated angle of facing for the robot in degrees on
	 *            [0,360)
	 */
	public void setRobotFacing(int team, int robot, double newFacing) {
		this.robotFacing[team][robot] = newFacing;
	}

	/**
	 * Set which pitch we're playing on (0 is Main, 1 is Side)
	 * 
	 * TODO: Should be abstracted
	 */
	public void setPitchId(int pitchId) {
		this.pitchId = pitchId;
	}

	/**
	 * Get which pitch we're playing on (0 is Main, 1 is Side)
	 * 
	 * TODO: Should be abstracted
	 */
	public int getPitchId() {
		return pitchId;
	}

	/**
	 * Set our team's colour (0 is yellow, 1 is blue)
	 * 
	 * TODO: Should be abstracted
	 */
	public void setOurColor(int ourColor) {
		this.ourColor = ourColor;
	}

	/**
	 * Get our team's colour (0 is yellow, 1 is blue)
	 * 
	 * TODO: Should be abstracted
	 */
	public int getOurColor() {
		return ourColor;
	}

	/**
	 * Gets the direction our team is supposed to shoot towards (0 is left)
	 * 
	 * TODO: Should be abstracted
	 */
	public int getDirection() {
		return shootingDirection;
	}

	/**
	 * Sets the direction our team is supposed to shoot towards (0 is left)
	 * 
	 * TODO: Should be abstracted
	 */
	public void setDirection(int direction) {
		this.shootingDirection = direction;
	}

	/**
	 * Setter method for the WorldStatePaint FPS. Useful for calculating the
	 * velocity of objects in real units.
	 */
	public void setPaintFps(int fps) {
		this.paintFps = fps;
	}

	/**
	 * Sets the current velocity of the ball in pixels per second. Also
	 * automatically calculates the ball speed and facing angle. (Speed is also
	 * in pixels per second)
	 */
	public void setBallVelocity(Point2 ballVelocity) {
		this.ballVelocity = ballVelocity;
		this.ballSpeed = ballVelocity.modulus();
		this.ballFacing = ballVelocity.angle();
	}

	/**
	 * Gets the current velocity of the ball in pixels per second.
	 */

	public Point2 getBallVelocity() {
		return ballVelocity;
	}

	/**
	 * Setter method for updating the estimated data from FutureBall
	 * 
	 * @param pt
	 */
	public void setIntersectData(Intersect inter) {
		this.futureData = inter;
	}

	/**
	 * Setter method for updating the data from FutureBall
	 * 
	 * @param pt
	 */
	public void setFutureData(Intersect pt) {
		this.futureData = pt;
	}

	/**
	 * Getter method for the data from FutureBall
	 * 
	 * @return
	 */
	public Intersect getFutureData() {
		return this.futureData;
	}

	/**
	 * Gets the current pitch object associated with this world state
	 */
	public Pitch getPitch() {
		return pitch;
	}

	/**
	 * Returns the quadrant of a point. Useful for figuring out which zone a
	 * ball or robot is in, or what quadrant an arbitrary point is in before you
	 * call goTo on the robot
	 * 
	 * @param q
	 * @return
	 */
	public int quadrantFromPoint(Point2 q) {
		int x = q.getX();
		ArrayList<Point2> points = pitch.getArrayListOfPoints();
		if (x < points.get(14).x) {
			return 0;
		} else if (x < points.get(2).x) {
			return 1;
		} else if (x < points.get(3).x) {
			return 2;
		} else if (x < points.get(4).x) {
			return 3;
		} else if (x < points.get(6).x) {
			return 4;
		}
		return 0;
	}

	/**
	 * Returns which quadrant the ball is currently in, where quadrant 1, 2, 3,
	 * 4 are the left defender, left attacker, right attacker, right defender
	 * slots, respectively.
	 * 
	 * <p />
	 * 
	 * A value of 0 is an unknown quadrant.
	 * 
	 * @return
	 */
	public int getBallQuadrant() {
		return quadrantFromPoint(getBallPosition());
	}
}
