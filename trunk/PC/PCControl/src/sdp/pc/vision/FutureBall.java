package sdp.pc.vision;

public class FutureBall {
	public static WorldState state = Vision.state;
	public static Point2 collision = new Point2(-1, -1);

	public static boolean contains(Point2 q) {
		if (Vision.hullPoints.containsKey(q.getX() * 1000 + q.getY())) {
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
				Vision.frameImage.getGraphics().drawLine((int)x, (int)y, pts[i].getX(),
						pts[i].getY());
			}
		}
		System.out.println(p[0] + p[1]);
	}

	public static Point2 estimateRealStopPoint() {
		double delX = state.getBallVelocity().getX(), delY = state
				.getBallVelocity().getY();
		Point2 pos = state.getBallPosition().copy();
		double tarX = pos.getX(), tarY = pos.getY();
		while (Math.abs(delX) > 0.25 || Math.abs(delY) > 0.25) {
			delX *= 0.6;
			delY *= 0.6;
			tarX -= delX;
			tarY -= delY;
		}
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
				if (Alg.pointInPitch(new Point2((int) sX, (int) sY))
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
}
