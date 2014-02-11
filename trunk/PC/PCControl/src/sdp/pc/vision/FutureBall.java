package sdp.pc.vision;

public class FutureBall {
	public static WorldState state = Vision.state;
	public static Point2 collision = new Point2(-1, -1);

	public static boolean contains(Point2 q) {
		// TODO: untested
		if (Vision.stateListener.pointInPitch(q)) {
			return true;
		}
		return false;
	}

	public static void collide8(double x, double y) {
		boolean[] q = new boolean[8];
		Point2[] pts = new Point2[8];

		pts[0] = new Point2((int) x + 1, (int) y);
		q[0] = contains(pts[0]);

		pts[1] = new Point2((int) x + 1, (int) y - 1);
		q[1] = contains(pts[1]);

		pts[2] = new Point2((int) x, (int) y - 1);
		q[2] = contains(pts[2]);

		pts[3] = new Point2((int) x - 1, (int) y - 1);
		q[3] = contains(pts[3]);

		pts[4] = new Point2((int) x - 1, (int) y);
		q[4] = contains(pts[4]);

		pts[5] = new Point2((int) x - 1, (int) y + 1);
		q[5] = contains(pts[5]);

		pts[6] = new Point2((int) x, (int) y + 1);
		q[6] = contains(pts[6]);

		pts[7] = new Point2((int) x + 1, (int) y + 1);
		q[7] = contains(pts[7]);

		boolean here = q[0];
		int[] p = new int[2];
		int found = 0;
		for (int i = 1; i < 8; i++) {
			if (!here == q[i]) {
				here = !here;
				p[found] = i;
				
			}
		}
		System.out.println("collide8");
		Vision.frameLabel.getGraphics().drawLine(pts[0].getX(), pts[0].getY(),
				pts[1].getX(), pts[1].getY());
	}

	public static Point2 estimateRealStopPoint() {
		Point2 vel = state.getBallVelocity();
		double delX = vel.getX(), delY = vel.getY();
		Point2 pos = state.getBallPosition().copy();
		double tarX = pos.getX(), tarY = pos.getY();
		// Changed this with the help of geometric series
		tarX -= delX * 1.5;
		tarY -= delY * 1.5;

		double distToStop = (new Point2((int) (tarX - pos.getX()),
				(int) (tarY - pos.getY())).modulus());
		double sX = pos.getX(), sY = pos.getY();
		double vHatX = tarX - pos.getX();
		double vHatY = tarY - pos.getY();
		vHatX /= (Math.sqrt(vHatX * vHatX + vHatY * vHatY));
		vHatY /= (Math.sqrt(vHatX * vHatX + vHatY * vHatY));
		collision = Point2.EMPTY;
		if (state.getBallVelocity().modulus() > 5) {
			while (collision.getX() == -1 && distToStop > 0) {
				if (Vision.stateListener.pointInPitch(new Point2((int) sX,
						(int) sY)) && !contains(new Point2((int) sX, (int) sY))) {
					collision = new Point2((int) sX, (int) sY);
					collide8(sX, sY);
				}
				sX += vHatX;
				sY += vHatY;
				distToStop -= Math.sqrt(vHatX + vHatY);
			}
		}
		return new Point2((int) tarX, (int) tarY);
	}
	
	public static double angleBetweenDiffs(double facing, double angle) {
		if (facing > 270) {
			facing = 360 - facing + angle;
		} else if (180 < facing && facing <= 270){
			facing = facing - 180 + angle;
		} else if (90 < facing && facing <= 180) {
			facing = 180 - facing - angle; 
		} else {
			facing = facing - angle;
		}
		return facing;
	}
	
	public static double movesInGoals(int def, double newY) {
		Point2 goal_top;
		Point2 goal_bottom;
		if (def == 0) {
			goal_top = state.leftGoalTop;
			goal_bottom = state.leftGoalBottom;
		} else {
			goal_top = state.rightGoalTop;
			goal_bottom = state.rightGoalBottom;
		}
		if (goal_top.getY() > newY || goal_bottom.getY() < newY) {
			newY = 0;
		}
		return newY;
	}

	public static Point2 estimateBallPositionWhen(Point2 position,
			double facing, Point2 robotPosition, int def) {
		
		//calculate the distance robot needs to move to cut off the ball
		double distBetweenObjectAndDefender = position.distance(robotPosition);
		double deltaX = Math.abs(position.getX() - robotPosition.getX());
		//System.out.println("Dist: " + distBetweenObjectAndDefender);
		//System.out.println("deltax: " + deltaX);
		double angle = Math.acos(deltaX/distBetweenObjectAndDefender);
		if (position.getY() < robotPosition.getY()) {
			angle = -angle;
		}
		//System.out.println("Sending angle: " + angle);
		angle = angleBetweenDiffs(facing, angle);
		//System.out.println("Facing: " + facing);
		//System.out.println("Angle: " + angle);
	
		double distanceToMove = distBetweenObjectAndDefender * Math.tan(angle);
		//System.out.println("Distance to move: " + distanceToMove);
		double newY;
		if (facing > 180) {
			newY = robotPosition.getY() + distanceToMove; 
		} else {
			newY = robotPosition.getY() - distanceToMove;
		}
		//check if object is facing toward goals area
		newY = movesInGoals(def, newY);
		//System.out.println(newY);
		return new Point2((int)robotPosition.getX(), 290);
	}
}
