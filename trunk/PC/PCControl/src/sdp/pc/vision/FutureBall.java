package sdp.pc.vision;

public class FutureBall {
	public static WorldState state = Vision.state;
	public static Point2 collision = new Point2(-1, -1);
	
	public static boolean contains(Point2 q) {
		//TODO: untested
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
				//TODO: untested
				Vision.frameLabel.getGraphics().drawLine((int)x, (int)y, pts[i].getX(),
						pts[i].getY());
			}
		}
		//System.out.println(p[0] + p[1]);
	}

	public static Point2 estimateRealStopPoint() {
		Point2 vel = state.getBallVelocity(); 
		double delX = vel.getX(), delY = vel.getY();
		Point2 pos = state.getBallPosition().copy();
		double tarX = pos.getX(), tarY = pos.getY();
		//Changed this with the help of geometric series
		tarX -= delX*1.5;
		tarY -= delY*1.5;
		
		double distToStop = (new Point2((int) (tarX - pos.getX()),
				(int) (tarY - pos.getY())).modulus());
		double sX = pos.getX(), sY = pos.getY();
		double vHatX = tarX - pos.getX();
		double vHatY = tarY - pos.getY();
		vHatX /= (Math.sqrt(vHatX * vHatX + vHatY * vHatY));
		vHatY /= (Math.sqrt(vHatX * vHatX + vHatY * vHatY));
		collision = new Point2(-1, -1);
		if (state.getBallVelocity().modulus() > 5) {
			while (collision.getX() == -1 && distToStop > 0) {
				if (Vision.stateListener.pointInPitch(new Point2((int) sX, (int) sY))
						&& !contains(new Point2((int) sX, (int) sY))) {
					collision = new Point2((int) sX, (int) sY);
					collide8(sX, sY);
				}
				sX += vHatX;
				sY += vHatY;
				// distToStop -= Math.sqrt(vHatX + vHatY);
			}
		}
		return new Point2((int) tarX, (int) tarY);
	}
	//TODO Implement this such that by given the position and facing of the attacker
	// and the X-value of the defender robot's position
	// estimateBallPositionWhen returns Y-value where the ball should be when robotPositionX == ballPosition.getX()
	public static double estimateBallPositionWhen(Point2 attPosition, double attFacing, double robotPositionX) {
		return 160.0;
	}
}