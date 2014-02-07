package sdp.pc.vision;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Point2 {
	private static final double STD_DEV_THRESHOLD = 1.17;

	private int x;
	private int y;
	
	/**
	 * Constructs a new point at the given coordinates
	 */
	public Point2(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Constructs a new point with coordinates (0,0)
	 */
	public Point2() {
		this.x = 0;
		this.y = 0;
	}

	/**
	 * Constructs a copy of a point
	 * 
	 * @param p
	 */
	public Point2(Point2 p) {
		this.x = p.x;
		this.y = p.y;
	}

	/**
	 * Returns a new instance of the point given (copies x,y)
	 * 
	 * @return
	 */
	public Point2 copy() {
		return new Point2(this.x, this.y);
	}

	/**
	 * Gets the x of the point
	 * 
	 * @return
	 */
	public int getX() {
		return x;
	}

	/**
	 * Sets the x of the point
	 * 
	 * @param x
	 */
	public void setX(int x) {
		this.x = x;
	}
	
	public double modulus(){
		return Math.sqrt(this.x*this.x + this.y*this.y);
	}

	/**
	 * Gets the y of the point
	 * 
	 * @return
	 */
	public int getY() {
		return y;
	}

	/**
	 * Sets the y of the point
	 * 
	 * @param y
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Gets a string representation of the point
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
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
			if (count > 0) {
				int cX = newX / count;
				int cY = newY / count;
				if (!(cX == 0 || cY == 0)) {
					this.x = cX;
					this.y = cY;
				}
			}
		}
	}

	/**
	 * Given a list of points and their centroid, removes all points further
	 * than const standard deviations from the centroid
	 * 
	 * @param points
	 * @param centroid
	 * @return
	 */
	public static ArrayList<Point2> removeOutliers(ArrayList<Point2> points,
			Point2 centroid) {
		ArrayList<Point2> goodPoints = new ArrayList<Point2>();

		if (points.size() > 0) {
			double stdDev = standardDeviation(points, centroid);

			// Remove points further than const standard deviations
			stdDev *= STD_DEV_THRESHOLD;
			for (Point2 q : points) {
				if (q.distance(centroid) < stdDev) {
					goodPoints.add(q);
				}
			}
		}

		return goodPoints;
	}

	/**
	 * Gives the standard deviation for a set of points, given their centroid.
	 * 
	 * @param points
	 * @param centroid
	 * @return
	 */
	public static double standardDeviation(ArrayList<Point2> points,
			Point2 centroid) {
		double variance = 0.0;

		for (int i = 0; i < points.size(); ++i) {
			variance += centroid.distanceSq(points.get(i));
		}

		return Math.sqrt(variance / (double) (points.size()));
	}

	/**
	 * Gets the squared distance to the given point
	 * 
	 * @param p
	 *            the point we want to find the distance to
	 * @return the squared distance to the point
	 */
	public double distanceSq(Point2 p) {
		return distanceSq(p.x, p.y);
	}

	/**
	 * Gets the squared distance to the given point represented by x/y
	 * coordinates
	 * 
	 * @param x
	 *            the x coordinate of the point
	 * @param y
	 *            the y coordinate of the point
	 * @return the squared distance to the point
	 */
	public double distanceSq(double x, double y) {
		double dx = x - this.getX();
		double dy = y - this.getY();
		return dx * dx + dy * dy;
	}

	/**
	 * Gets the distance to the given point
	 * 
	 * @param p
	 *            the point we want to find the distance to
	 * @return the distance to the point
	 */
	public double distance(Point2 p) {
		return Math.sqrt(distanceSq(p.x, p.y));
	}

	/**
	 * Gets the distance to the given point
	 * 
	 * @param p
	 *            the point we want to find the distance to
	 * @return the distance to the point
	 */
	public double distance(Point2D.Double p) {
		return Math.sqrt(distanceSq(p.x, p.y));
	}

	/**
	 * Gets a point which is the difference between this point and the given
	 * point
	 * 
	 * @param p
	 *            the point to subtract from this
	 * @return a point with coordinates (x-p.x; y-p.y)
	 */
	public Point2 subtract(Point2 p) {
		return new Point2(x - p.x, y - p.y);
	}

	/**
	 * Gets a point which is the sum of this point and the given point
	 * 
	 * @param p
	 *            the point to add to this
	 * @return a point with coordinates (x+p.x; y+p.y)
	 */
	public Point2 add(Point2 p) {
		return new Point2(this.x + p.x, this.y + p.y);
	}

	/**
	 * Gets a point which is the sum of this point and the given point
	 * 
	 * @param p
	 *            the point to add to this
	 * @return a point with coordinates (x+p.x; y+p.y)
	 */
	public Point2D.Double add(Point2D.Double p) {
		return new Point2D.Double(x + p.x, y + p.y);
	}

	/**
	 * Negatively scales (i.e. divides) this point by the given value
	 * 
	 * @param divisor
	 *            the value to divide by
	 * @return a point with coordinates (x/p.x; y/p.y)
	 */
	public Point2 div(int divisor) {
		return new Point2(x / divisor, y / divisor);
	}

	/**
	 * Positively scales (i.e. multiplies) this point by the given value
	 * 
	 * @param multiplier
	 *            the value to multiply by
	 * @return a point with coordinates (x*p.x; y*p.y)
	 */
	public Point2 mult(int multiplier) {
		return new Point2(x * multiplier, y * multiplier);
	}

	/**
	 * Vector operation Returns a vector perpendicular to this one
	 * 
	 * @return a point with coordinates (y; x)
	 */
	public Point2 getPerpendicular() {
		return new Point2(y, x);
	}

	/**
	 * Gets a Point2D representation of this point
	 * 
	 * @return
	 */
	public Point2D.Double toDouble() {
		return new Point2D.Double(x, y);
	}

	/**
	 * Returns the intersection of the two lines defined by the 4 points
	 * 
	 * @param la
	 *            the first point of the first line
	 * @param las
	 *            the second point of the first line
	 * @param lb
	 *            the first point of the second line
	 * @param lbs
	 *            the second point of the second line
	 * @return the unique point which lies on both (la,las) and (lb,lbs)
	 */
	public static Point2D.Double getLinesIntersection(Point2 la, Point2 las,
			Point2 lb, Point2 lbs) {
		double a1 = las.y, b1 = -las.x, c1 = a1 * la.x + b1 * la.y, a2 = lbs.y, b2 = -las.x, c2 = a2
				* lb.x + b2 * lb.y;
		double delta = a1 * b2 - a2 * b1;
		if (delta == 0)
			return new Point2D.Double();
		return new Point2D.Double(((b2 * c1 - b1 * c2) / delta), ((a1 * c2 - a2
				* c1) / delta));
	}

	public double angleTo(Point2 p) {
		return Math.atan2(p.y - y, p.x - x);
	}

	public boolean isToLeft(Point2 a, Point2 b) {
		int dot = ((b.x - a.x) * (y - a.y) - (b.y - a.y) * (x - a.x));
		return dot > 0 || (dot == 0 && a.distanceSq(b) < a.distanceSq(this));
	}
}
