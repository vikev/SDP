package sdp.pc.vision;

import java.awt.geom.Point2D;
import java.util.ArrayList;


public class Point2 {
	private int x;
	private int y;

	public Point2() { this(0, 0); }

	public Point2(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Point2(Point2 p) {
		this.x = p.x;
		this.y = p.y;
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
		if (distanceSq(new Point2(oldX, oldY)) < fix_treshold) {
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
	public void filterPoints(ArrayList<Point2> points) {
		if (points.size() > 0) {
			double stdev = standardDeviation(points, this);

			int count = 0;
			int newX = 0;
			int newY = 0;

			// Remove points further than standard deviation
			for (int i = 0; i < points.size(); ++i) {
				Point2 p = points.get(i);

				if (Math.sqrt(this.distanceSq(p)) < stdev) {
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

	public static ArrayList<Point2> removeOutliers(
			ArrayList<Point2> points, Point2 centroid) {
		ArrayList<Point2> goodPoints = new ArrayList<Point2>();

		if (points.size() > 0) {
			double stdDev = standardDeviation(points, centroid);
			// Remove points further than 1.17 standard deviations
			stdDev *= 1.17;
			for (int i = 0; i < points.size(); ++i) {
				Point2 p = points.get(i);
				if (Math.sqrt(p.distanceSq(centroid)) < stdDev)
					goodPoints.add(p);
			}
		}

		return goodPoints;
	}

	public static double standardDeviation(ArrayList<Point2> points,
			Point2 centroid) {
		double variance = 0.0;

		for (int i = 0; i < points.size(); ++i) {
			variance += centroid.distanceSq(points.get(i));
		}

		return Math.sqrt(variance / (double) (points.size()));
	}

	public double distanceSq(Point2 p) {
		return distanceSq(p.x, p.y);
	}
	
	public double distanceSq(double x, double y) {
		double dx = x - getX();
		double dy = y - getY();
		return dx * dx + dy * dy;
	}

	public double distance(Point2 p) {
		return Math.sqrt(distanceSq(p.x, p.y));
	}
	
	public double distance(Point2D.Double p) {
		return Math.sqrt(distanceSq(p.x, p.y));
	}
	
	public Point2 subtract(Point2 p) {
		return new Point2(x - p.x, y - p.y);
	}
	
	public Point2 add(Point2 p) {
		return new Point2(x + p.x, y + p.y);
	}
	
	public Point2D.Double add(Point2D.Double p) {
		return new Point2D.Double(x + p.x, y + p.y);
	}
	
	public Point2 div(int divisor) {
		return new Point2(x / divisor, y / divisor);
	}
	
	public Point2 mult(int multiplier) {
		return new Point2(x * multiplier, y * multiplier);
	}
	
	public Point2 getPerpendicular() {
		return new Point2(y, x);
	}
	
	public Point2D.Double toDouble() {
		return new Point2D.Double(x, y);
	}
	
	public static Point2D.Double getLinesIntersection(Point2 la, Point2 las, Point2 lb, Point2 lbs) {
		double a1 = las.y,
			   b1 = -las.x,
			   c1 = a1*la.x+b1*la.y,
			   a2 = lbs.y,
			   b2 = -las.x,
			   c2 = a2*lb.x + b2*lb.y;
		double delta = a1*b2 - a2*b1;
		if(delta == 0)
			return new Point2D.Double();
		return new Point2D.Double(((b2*c1-b1*c2)/delta), ((a1*c2-a2*c1)/delta));
	}
}