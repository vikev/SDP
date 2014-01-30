package sdp.pc.vision;


public class WorldState {
	final static int playersPerTeam = 2;
	final static int nTeams = 2;
	
	public static final int TEAM_YELLOW = 0;
	public static final int TEAM_BLUE = 1;
	public static final Point2 leftGoalCentre = new Point2 (77,235); //Taken from image of pitch
	public static final Point2 rightGoalCentre = new Point2 (589,241);
	public int targetGoal = 1; // 1 is left goal 0 is right goal
	
	
	private int direction; // 0 = right, 1 = left.
	
	private Point2 ball;
	
	private Point2[][] robotLoc = new Point2[nTeams][playersPerTeam];
	
	private double[][] robotFacing = new double[nTeams][playersPerTeam];

	public WorldState() {

		/* control properties */
		this.direction = 0;

		/* object properties */
		for(int t = 0; t < nTeams; t++)
			for(int p = 0; p < playersPerTeam; p++)
				robotLoc[t][p] = new Point2();
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

	public Point2 getRobotPosition(int team, int robot) {
		return robotLoc[team][robot];
	}

	/***
	 * Updates the team position of a given robot, part of a given team
	 * @param team the team of the updated robot
	 * @param robot the robot to update
	 * @param newLoc the updated position of the robot
	 */
	public void setRobotPosition(int team, int robot, Point2 newLoc) {
		this.robotLoc[team][robot] = newLoc;
	}

	public double getRobotFacing(int team, int robot) {
		return robotFacing[team][robot];
	}

	/**
	 * Updates the facing of a given robot, part of a given team
	 * @param team the team of the updated robot
	 * @param robot the robot to update
	 * @param newFacing the updated angle of facing for the robot
	 */
	public void setRobotFacing(int team, int robot, double newFacing) {
		this.robotFacing[team][robot] = newFacing;
	}

	public int getDirection() {
		return direction;
	}
	public int getTargetGoal(){
		return targetGoal;
	}
	public void setTargetGoal(int newTargetGoal){
		targetGoal = newTargetGoal;
	}
	public Point2 getLeftGoalCentre(){
		return leftGoalCentre;
	}
	public Point2 getRightGoalCentre(){
		return rightGoalCentre;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

}