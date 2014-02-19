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

		// TODO: I can find a trivial example of this algorithm proceeding
		// infinitely!
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

	// We deprecated this method. I commented it out and nothing seems to be
	// using it. Code can be deleted.

	// /**
	// * Calculates the length of a line between two points, i.e. the distance
	// * between the points
	// *
	// * @param a
	// * - the first point
	// * @param b
	// * - the second point
	// * @return distance between a and b
	// * @deprecated Use a.distance(b) instead
	// */
	// public static double lineSize(Point2 a, Point2 b) {
	// double s = (double) (b.getX() - a.getX());
	// double t = (double) (b.getY() - b.getY());
	// return Math.sqrt(s * s + t * t);
	// }

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
	public static LinkedList<Point2> convexHull(ArrayList<Point2> pts) {
		if (pts.size() > 0) {
			Point2 pHull = pts.get(0);
			Point2 endPoint;

			LinkedList<Point2> p = new LinkedList<Point2>();

			// Get the left-most point
			for (Point2 pt : pts)
				if (pt.getX() < pHull.getX())
					pHull = pt;

			// Iterate through the points
			do {
				p.add(pHull);
				endPoint = pts.get(0);
				for (int j = 1; j < pts.size(); j++)
					if (endPoint == pHull
							|| (pts.get(j).isToLeft(p.getLast(), endPoint)))
						endPoint = pts.get(j);
				pHull = endPoint;
			} while (endPoint != p.get(0));

			return p;
		}
		System.out.println("Empty hull!");
		return new LinkedList<Point2>();
	}

	/**
	 * Checks if a point is inside a convex polygon represented by a linked list
	 * of its vertices, usually calculated using the convexHull() method.
	 * Implementation taken from an answer in StackOverflow.
	 * 
	 * @param borderPoints
	 *            - points on the polygon's border
	 * @param pointX
	 *            - the x co-ordinate of the point to check
	 * @param pointY
	 *            - the y co-ordinate of the point to check
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
	 * Auxillary method for getIntervals method
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
