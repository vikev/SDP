package sdp.pc.vision;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Class containing various (and arbitrary) algorithms for static analysis of
 * state, video feed, data, etc.
 */
public class Alg {

	/**
	 * A double comparator using an epsilon threshold. If a is less than b,
	 * returns -1, if a is greater than b, returns 1, otherwise returns 0.
	 * 
	 * @param a
	 * @param b
	 * @param eps
	 *            - epsilon threshold with which to compare
	 * @return
	 */
	public static int doubleComparator(double a, double b, double eps) {
		double diff = b - a;
		double modu = Math.abs(diff);
		if (modu < eps) {
			return 0;
		} else if (diff < 0) {
			return -1;
		} else {
			return 1;
		}
	}

	public static Point2 getCentroid(LinkedList<Point2> vertices) {
		double x = 0, y = 0, k = vertices.size();
		for (Point2 q : vertices) {
			x += q.getX();
			y += q.getY();
		}
		x /= k;
		y /= k;
		return new Point2((int) x, (int) y);
	}

	public static boolean inMinorHull(LinkedList<Point2> vertices,
			double minorRadius, Point2 check) {
		return inMinorHullWeighted(vertices, minorRadius, check, 1.0, 1.0);
	}

	public static boolean inMinorHullWeighted(LinkedList<Point2> vertices,
			double minorRadius, Point2 check, double xWeight, double yWeight) {
		Point2 centre = getCentroid(vertices);
		LinkedList<Point2> n = new LinkedList<Point2>();
		for (Point2 q : vertices) {
			double rad = (q.angleTo(centre) + 360.0) * Math.PI / 180.0;

			double xOffs = xWeight * minorRadius * Math.cos(rad);
			double yOffs = yWeight * minorRadius * Math.sin(rad);
			n.add(q.add(new Point2((int) xOffs, (int) yOffs)));
		}
		// double distortion = Robot.getDistortion(check);
		// check = ((check.offset(distortion * 7,
		// check.angleTo(Vision.getCameraCentre()))));
		return isInHull(n, check);
	}

	/**
	 * Returns an angle ang in degrees on [0,360).
	 * 
	 * @param ang
	 *            angle in degrees
	 * @return ang equivalent on [0,360)
	 */
	public static double normalizeToUnitDegrees(double ang) {
		while (ang < 0.0) {
			ang += 360.0;
		}
		while (ang >= 360.0) {
			ang -= 360.0;
		}
		return ang;
	}

	/**
	 * Returns an angle ang in degrees on [-180,180), useful for comparing
	 * angles.
	 * 
	 * @param ang
	 *            angle in degrees
	 * @return ang equivalent on [-180, 180)
	 */
	public static double normalizeToBiDirection(double ang) {
		while (ang < -180.0) {
			ang += 360.0;
		}
		while (ang >= 180.0) {
			ang -= 360.0;
		}
		return ang;
	}

	/**
	 * Computes the smallest circle that contains the given points
	 * 
	 * @param pts
	 *            the points to enclose
	 * @return the minimum-radius circle enclosing them
	 */
	public static Circle getSmallestCircle(ArrayList<Point2> pts) {
		return minCircle(pts.size(), pts, 0, new Point2[3]);
	}

	/**
	 * Computes the smallest circle enclosing the desired points such that the
	 * boundary points are on its boundary
	 * 
	 * @param desiredPoints
	 *            the amount of points in p
	 * @param points
	 *            the points enclosed in the circle
	 * @param boundaryPointCount
	 *            the amount of points in b, should not exceed 2
	 * @param boundaryPoints
	 *            the points on the circle boundary
	 * @return
	 */
	private static Circle minCircle(int desiredPoints,
			ArrayList<Point2> points, int boundaryPointCount,
			Point2[] boundaryPoints) {
		Circle minC;

		// The circle is instantiated differently depending on the number of
		// points on the boundary. Note the default case assumes there are three
		// boundary points (!)
		switch (boundaryPointCount) {
		case 0:
			minC = new Circle(new Point2D.Double(-1, -1), 0);
			break;
		case 1:
			minC = new Circle(boundaryPoints[0].toDouble(), 0);
			break;
		case 2:
			minC = new Circle(boundaryPoints[0].add(boundaryPoints[1]).div(2)
					.toDouble(),
					boundaryPoints[0].distance(boundaryPoints[1]) / 2);
			break;
		default:
			return new Circle(boundaryPoints[0], boundaryPoints[1],
					boundaryPoints[2]);
		}

		// If the desired points are not all included in the minimum circle, the
		// function recurses and increases its boundary size.
		for (int i = 0; i < desiredPoints; i++)
			if (points.get(i).distance(minC.getPosition()) > minC.getRadius()) {
				// ... Compute B <--- B union P[i].
				boundaryPoints[boundaryPointCount] = points.get(i).copy();

				// ... Recurse
				minC = minCircle(i, points, boundaryPointCount + 1,
						boundaryPoints);
			}
		return minC;
	}

	/**
	 * Checks if the given value is within a certain range (epsilon) of the
	 * target value. To be used for pixel thresholding.
	 * 
	 * @param value
	 *            - the value we want to check
	 * @param target
	 *            - the target value, i.e. middle of the range
	 * @param epsilon
	 *            - the range around the target value
	 * @return True if 'value' is within 'epsilon'-range of 'target', false
	 *         otherwise
	 */
	public static boolean withinBounds(float value, float target, float epsilon) {
		return (Math.abs(value - target) < epsilon);
	}

	/**
	 * Given a set of points, return a linked list containing the vertices of
	 * the smallest convex polygon that can be fit around those points
	 * 
	 * @param pts
	 * @return a LinkedList of Point2's, containing the vertices of the
	 *         surrounding polygon
	 */
	public synchronized static LinkedList<Point2> convexHull(
			ArrayList<Point2> pts) {
		if (pts.size() > 0) {
			Point2 pHull = pts.get(0);
			Point2 endPoint;
			int maxIterations = 0;

			LinkedList<Point2> p = new LinkedList<Point2>();

			// Get the left-most point
			for (Point2 pt : pts)
				if (pt.getX() < pHull.getX())
					pHull = pt;
			// Iterate through the points
			do {
				maxIterations++;
				p.add(pHull);
				endPoint = pts.get(0);
				for (int j = 1; j < pts.size(); j++) {
					boolean h2l = hullIsToLeft(pts.get(j), p.getLast(),
							endPoint);
					if (endPoint == pHull || h2l)
						endPoint = pts.get(j);
				}
				pHull = endPoint;
			} while (endPoint != p.get(0) && maxIterations < 100);

			return p;
		}
		System.out.println("Empty hull!");
		return new LinkedList<Point2>();
	}

	/**
	 * Auxiliary method for the isInHull algorithm. Used for calculating if
	 * point q is to the polar "left" of a line between a and b.
	 * 
	 * @param q
	 *            - test subject
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean hullIsToLeft(Point2 q, Point2 a, Point2 b) {
		// Calculate using algorithm
		int dot = ((b.x - a.x) * (q.y - a.y) - (b.y - a.y) * (q.x - a.x));
		return dot > 0 || (dot == 0 && a.distanceSq(b) < a.distanceSq(q));
	}

	/**
	 * Checks if a point is inside a convex polygon represented by a linked list
	 * of its vertices, usually calculated using the convexHull() method.
	 * Implementation taken from an answer in StackOverflow.
	 * 
	 * @param borderPoints
	 *            - points on the polygon's border
	 * @param pointX
	 *            - the x coordinate of the point to check
	 * @param pointY
	 *            - the y coordinate of the point to check
	 * @return true if the point is within the polygon, false otherwise
	 */
	public static boolean isInHull(LinkedList<Point2> borderPoints, int pointX,
			int pointY) {
		int i;
		int j;
		boolean result = false;
		for (i = 0, j = borderPoints.size() - 1; i < borderPoints.size(); j = i++) {
			if ((borderPoints.get(i).y > pointY) != (borderPoints.get(j).y > pointY)
					&& (pointX < (borderPoints.get(j).x - borderPoints.get(i).x)
							* (pointY - borderPoints.get(i).y)
							/ (borderPoints.get(j).y - borderPoints.get(i).y)
							+ borderPoints.get(i).x)) {
				result = !result;
			}
		}
		return result;
	}

	/**
	 * Checks if a point is inside a convex polygon represented by a linked list
	 * of its vertices, usually calculated using the convexHull() method.
	 * Implementation taken from an answer in StackOverflow.
	 * 
	 * @param borderPoints
	 *            - points on the polygon's border
	 * @param p
	 *            - the point to check
	 * @return true if the point is within the polygon, false otherwise
	 */
	public static boolean isInHull(LinkedList<Point2> borderPoints, Point2 p) {
		return isInHull(borderPoints, p.x, p.y);
	}
	
	public static Point2 correspondingOnHull(LinkedList<Point2> borderPoints, Point2 p) {
		int i;
		int j;
		double intDist = 0;
		Point2 intPoint = Point2.EMPTY;
		for (i = 0; i < borderPoints.size() - 1; i++) {
			Point2 norm = borderPoints.get(i+1).sub(borderPoints.get(i));
			Point2 bP = Point2.getPerpendicular(norm);
			Point2 rP = p.sub(borderPoints.get(i));
			Point2 rP2 = rP.add(bP);
			Point2D.Double inter = Point2.getLinesIntersection(Point2.EMPTY, norm, rP, rP2);
			Point2 point = new Point2(inter);
			Point2 intercept = point.sub(borderPoints.get(i));
			double dist = intercept.distance(p);
			if (dist < intDist) {
				intDist = dist;
				intPoint = point;
			}
			
		}
		return intPoint;
	}

	/**
	 * Splits the given array into intervals based on the margins (thresholds)
	 * given The returned array lists contain Point2's whose X values represent
	 * the start of the interval, and Y values represent its end. The index of
	 * the array list represents the class of the interval
	 * 
	 * @param vals
	 *            the values to find the intervals in
	 * @param minSize
	 *            the minimum size of an interval
	 * @param margins
	 *            the minimum value for an item to get to the i'th interval
	 *            (ascending order)
	 * @return An array of lists with length equal to the margin count and each
	 *         containing the intervals where such points are contained, as
	 *         points (x - start, y - end)
	 */
	public static ArrayList<Point2>[] getIntervals(int[] vals, int minSize,
			int... margins) {

		@SuppressWarnings("unchecked")
		// no generic arrays
		ArrayList<Point2>[] ans = new ArrayList[margins.length];

		// create margins
		for (int i = 0; i < margins.length; i++)
			ans[i] = new ArrayList<Point2>();

		// vars
		int state = 0, newState = 0, stateStart = 0;
		for (int i = 0; i < vals.length; i++) {
			newState = getIntervalState(margins, vals[i]);

			// System.out.println(i + "   " + vals[i] + "  " + newState);

			// unchanged state
			if (state == newState)
				continue;

			// add to answers if state was valid (> 0)
			if (state > 0 && i - stateStart > minSize) {
				ans[state - 1].add(new Point2(stateStart, i - 1));
			}
			stateStart = i;

			state = newState;
		}
		return ans;
	}

	/**
	 * Auxiliary method for getIntervals method
	 * 
	 * @param margins
	 * @param val
	 * @return
	 */
	private static int getIntervalState(int[] margins, int val) {
		int i = 0;
		while (i < margins.length && margins[i] < val)
			i++;
		return i;
	}

}
