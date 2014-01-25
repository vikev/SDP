package sdp.pc.vision;

import java.util.ArrayList;


public class Position {
	private int x;
	private int y;


	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	
	public void setX(int x) {
		this.x = x;
	}


	public int getY() {
		return y;
	}


	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	
	public void fixValues(int oldX, int oldY) {
		// Use old values if nothing found
		if (this.getX() == 0) {
			this.setX(oldX);
		}
		if (this.getY() == 0) {
			this.setY(oldY);
		}

		// Use old values if not changed much
		if (sqrdEuclidDist(this, new Position(oldX, oldY)) < 9) {
			this.setX(oldX);
			this.setY(oldY);
		}
	}

	/**
	 * Updates the centre point of the object, given a list of new points to
	 * compare it to. Any points too far away from the current centre are
	 * removed, then a new mean point is calculated and set as the centre point.
	 * 
	 * @param points
	 *            The set of points
	 */
	public void filterPoints(ArrayList<Position> points) {
		if (points.size() > 0) {
			double stdev = standardDeviation(points, this);

			int count = 0;
			int newX = 0;
			int newY = 0;

			// Remove points further than standard deviation
			for (int i = 0; i < points.size(); ++i) {
				Position p = points.get(i);

				if (Math.sqrt(sqrdEuclidDist(this, p)) < stdev) {
					newX += p.getX();
					newY += p.getY();
					++count;
				}
			}

			int oldX = this.getX();
			int oldY = this.getY();

			if (count > 0) {
				this.setX(newX / count);
				this.setY(newY / count);
			}

			this.fixValues(oldX, oldY);
		}
	}


	public static ArrayList<Position> removeOutliers(
			ArrayList<Position> points, Position centroid) {
		ArrayList<Position> goodPoints = new ArrayList<Position>();

		if (points.size() > 0) {
			double stdDev = standardDeviation(points, centroid);
			// Remove points further than 1.17 standard deviations
			stdDev *= 1.17;
			for (int i = 0; i < points.size(); ++i) {
				Position p = points.get(i);
				if (Math.sqrt(sqrdEuclidDist(centroid, p)) < stdDev)
					goodPoints.add(p);
			}
		}

		return goodPoints;
	}

	public static double standardDeviation(ArrayList<Position> points,
			Position centroid) {
		double variance = 0.0;

		// Standard deviation
		for (int i = 0; i < points.size(); ++i) {
			variance += sqrdEuclidDist(points.get(i), centroid);
		}

		return Math.sqrt(variance / (double) (points.size()));
	}

	public static int sqrdEuclidDist(Position p1, Position p2) {
		int xDiff = p2.getX() - p1.getX();
		int yDiff = p2.getY() - p1.getY();

		return xDiff * xDiff + yDiff * yDiff;
	}
}
