package sdp.pc.vision;

public class FutureBall {
	public static WorldState state = Vision.state;
	public static Point2 collision = new Point2(-1, -1);

	public static boolean contains(Point2 q) {
		if (Vision.points[q.getX()][q.getY()] != null
				&& Vision.points[q.getX()][q.getY()].getX() > 0) {
			return true;
		}
		return false;
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
		double sX = pos.getX(), sY = pos.getY(), vHatX = tarX
				/ Math.sqrt(tarX * tarX + tarY * tarY), vHatY = tarY
				/ Math.sqrt(tarX * tarX + tarY * tarY);
		// for (Point2 s = new Point2((int) sX, (int) sY); (s.getX() < tarX && s
		// .getY() < tarY); s.add(new Point2((int) vHatX, (int) vHatY))) {
		while (true) {
			if (Math.abs(sX) > 800 || Math.abs(sY) > 800)
				break;
			if (sX>0 && sY>0 && sX < 800 && sY < 800
					&& !contains(new Point2((int) sX, (int) sY))) {
				collision = new Point2((int) sX, (int) sY);
			}
			sX += vHatX;
			sY += vHatY;
		}
		return new Point2((int) tarX, (int) tarY);
	}
}
