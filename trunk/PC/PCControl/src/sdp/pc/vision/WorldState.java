package sdp.pc.vision;


public class WorldState {
	final static int playersPerTeam = 2;
	final static int nTeams = 2;
	
	public static final int TEAM_YELLOW = 0;
	public static final int TEAM_BLUE = 1;
	
	
	private int direction; // 0 = right, 1 = left.
	
	private Point2 ball;
	
	private Point2[][] teamLoc = new Point2[nTeams][playersPerTeam];
	
	private double[][] teamFacing = new double[nTeams][playersPerTeam];

	public WorldState() {

		/* control properties */
		this.direction = 0;

		/* object properties */
		for(int t = 0; t < nTeams; t++)
			for(int p = 0; p < playersPerTeam; p++)
				teamLoc[t][p] = new Point2();
	}
	
	public Point2 getBallPosition() {
		return ball;
	}

	/***
	 * Sets the 
	 * @param newPos
	 */
	public void setBallPosition(Point2 newPos) {
		this.ball = newPos;
	}

	public Point2 getTeamPosition(int team, int robot) {
		return teamLoc[team][robot];
	}

	/***
	 * Updates the team position of a given robot, part of a given team
	 * @param team the team of the updated robot
	 * @param robot the robot to update
	 * @param newLoc the updated position of the robot
	 */
	public void setTeamPosition(int team, int robot, Point2 newLoc) {
		this.teamLoc[team][robot] = newLoc;
	}

	public double getTeamFacing(int team, int robot) {
		return teamFacing[team][robot];
	}

	/**
	 * Updates the facing of a given robot, part of a given team
	 * @param team the team of the updated robot
	 * @param robot the robot to update
	 * @param newFacing the updated angle of facing for the robot
	 */
	public void setTeamFacing(int team, int robot, double newFacing) {
		this.teamFacing[team][robot] = newFacing;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

}