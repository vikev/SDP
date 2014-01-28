package sdp.pc.vision;

import java.util.ArrayList;


public class Position {
	private int x;
	private int y;

	public Position() { this(0, 0); }

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

	static final int fix_treshold = 9;
	public void fixValues(int oldX, int oldY) {
		// Use old values if nothing found
		if (x == 0)
			x = oldX;
		if (y == 0)
			y = oldY;

		// Use old values if not changed much
		if (getDistanceSq(new Position(oldX, oldY)) < fix_treshold) {
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

				if (Math.sqrt(this.getDistanceSq(p)) < stdev) {
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
				if (Math.sqrt(p.getDistanceSq(centroid)) < stdDev)
					goodPoints.add(p);
			}
		}

		return goodPoints;
	}

	public static double standardDeviation(ArrayList<Position> points,
			Position centroid) {
		double variance = 0.0;

		for (int i = 0; i < points.size(); ++i) {
			variance += centroid.getDistanceSq(points.get(i));
		}

		return Math.sqrt(variance / (double) (points.size()));
	}

	public int getDistanceSq(Position p) {
		return getDistanceSq(p.getX(), p.getY());
	}
	
	public int getDistanceSq(int x, int y) {
		int dx = x - getX();
		int dy = y - getY();
		return dx * dx + dy * dy;
	}
	
	public double getDistance(Position p) {
		return Math.sqrt(getDistanceSq(p));
	}
}
