package sdp.pc.vision;


public class WorldState {

	private int direction; // 0 = right, 1 = left.
	private int yellowX;
	private int yellowY;
	private int ballX;
	private int ballY;
	private double yellowOrientation;

	public WorldState() {

		/* control properties */
		this.direction = 0;

		/* object properties */
		this.yellowX = 0;
		this.yellowY = 0;
		this.ballX = 0;
		this.ballY = 0;
		this.yellowOrientation = 0;
	}

	public int getBallX() {
		return ballX;
	}

	public int getBallY() {
		return ballY;
	}

	public int getYellowX() {
		return yellowX;
	}

	public void setYellowX(int yellowX) {
		this.yellowX = yellowX;
	}

	public int getYellowY() {
		return yellowY;
	}

	public void setYellowY(int yellowY) {
		this.yellowY = yellowY;
	}

	public void setBallX(int ballX) {
		this.ballX = ballX;
	}

	public void setBallY(int ballY) {
		this.ballY = ballY;
	}
	
	public double getYellowOrientation() {
		return yellowOrientation;
	}

	public void setYellowOrientation(double yellowOrientation) {
		this.yellowOrientation = yellowOrientation;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

}