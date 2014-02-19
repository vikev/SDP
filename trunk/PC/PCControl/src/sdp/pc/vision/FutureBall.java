package sdp.pc.vision;

/**
 * Class for estimating the real trajectories of the ball. Feeds data it
 * estimates to the world state. FutureBall is static and monolithic and
 * shouldn't be instantiated.
 * 
 * @author s1143704
 * 
 */
public class FutureBall {

	/**
	 * The world state attached to FutureBall.
	 */
	public static WorldState state = Vision.state;

	/**
	 * The estimated collision point. For now, there is at most one collision.
	 */
	public static Point2 collision = Point2.EMPTY;

	/**
	 * Returns true if the pitch contains point q
	 * 
	 * @param q
	 * @return
	 */
	public static boolean pitchContains(Point2 q) {
		// TODO: untested
		if (Vision.stateListener.pointInPitch(q)) {
			return true;
		}
		return false;
	}

	/**
	 * Calculate if the pitch contains the 8 surrounding pixels around (x,y) and
	 * therefore determine the deflection angle.
	 * 
	 * TODO: Incomplete and untested
	 * 
	 * @param x
	 * @param y
	 */
	public static void collide8(double x, double y) {
		boolean[] q = new boolean[8];
		Point2[] pts = new Point2[8];

		pts[0] = new Point2((int) x + 1, (int) y);
		q[0] = pitchContains(pts[0]);

		pts[1] = new Point2((int) x + 1, (int) y - 1);
		q[1] = pitchContains(pts[1]);

		pts[2] = new Point2((int) x, (int) y - 1);
		q[2] = pitchContains(pts[2]);

		pts[3] = new Point2((int) x - 1, (int) y - 1);
		q[3] = pitchContains(pts[3]);

		pts[4] = new Point2((int) x - 1, (int) y);
		q[4] = pitchContains(pts[4]);

		pts[5] = new Point2((int) x - 1, (int) y + 1);
		q[5] = pitchContains(pts[5]);

		pts[6] = new Point2((int) x, (int) y + 1);
		q[6] = pitchContains(pts[6]);

		pts[7] = new Point2((int) x + 1, (int) y + 1);
		q[7] = pitchContains(pts[7]);

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

	/**
	 * Estimates ball stop point given velocity and position of the ball
	 * 
	 * @return predicted ball position
	 */
	public static Point2 estimateRealStopPoint() {
		Point2 vel = state.getBallVelocity();
		Point2 pos = state.getBallPosition().copy();
		return estimateStopPoint(vel, pos);
	}

	/**
	 * Estimates object stop point given velocity and position of the object
	 * 
	 * @param vel
	 * @param pos
	 * @return predicted position
	 */
	public static Point2 estimateStopPoint(Point2 vel, Point2 pos) {
		double delX = vel.getX(), delY = vel.getY();
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
		if (vel.modulus() > 5) {
			while (collision.getX() == -1 && distToStop > 0) {
				if (Vision.stateListener.pointInPitch(new Point2((int) sX,
						(int) sY))
						&& !pitchContains(new Point2((int) sX, (int) sY))) {
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

	/**
	 * Estimates moving object position when movingPos.getX() ==
	 * staticPos.getX(). Return Point2(0,0) if movingPos.getX() never equals
	 * staticPos.getX()
	 * 
	 * @param movingPos
	 * @param movingFacing
	 * @param staticPoints
	 * @return estimated moving object position
	 */
	public static Point2 estimatePositionWhen(Point2 movingPos,
			double movingFacing, Point2 staticPos) {
		// Add some huge velocity for x
		int x = 1000;
		double angle = movingFacing;
		if (90 < angle && angle < 180) {
			angle = 180 - angle;
		} else if (180 < angle && angle < 270) {
			angle -= 180;
		} else if (angle > 270) {
			angle = 360 - angle;
		}
		int y = (int) (x * Math.tan(angle * Math.PI / 180));
		if (movingFacing < 180) {
			y = -y;
		}
		if (movingFacing > 270 || movingFacing < 90) {
			x = -x;
		}

		Point2 stopPos = FutureBall.estimateStopPoint(new Point2(x, y),
				movingPos);

		double deltaY = Math.abs(stopPos.getY() - movingPos.getY())
				* Math.abs(staticPos.getX() - movingPos.getX())
				/ Math.abs(stopPos.getX() - movingPos.getX());

		double predY;
		if (movingFacing < 180) {
			predY = deltaY + movingPos.getY();
		} else {
			predY = movingPos.getY() - deltaY;
		}
		// TODO check if point is within the boundaries and if not
		// return (0,0)
		return new Point2(staticPos.getX(), (int) predY);
	}
}
