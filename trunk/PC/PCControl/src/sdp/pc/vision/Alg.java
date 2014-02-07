package sdp.pc.vision;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

import sdp.pc.common.Constants;

/**
 * Class containing various (and arbitrary) algorithms for static analysis of
 * state, video feed, data, etc. 
 */
public class Alg {

	private Alg() { }	//no instantiation
	
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
	 * Computes the smallest circle enclosing the n points in p such that the m
	 * points in B are on its boundary
	 * 
	 * @param n
	 *            the amount of points in p
	 * @param p
	 *            the points enclosed in the circle
	 * @param m
	 *            the amount of points in b
	 * @param b
	 *            the points on the circle boundary
	 * @return
	 */
	private static Circle minCircle(int n, ArrayList<Point2> p, int m,
			Point2[] b) {
		Circle minC;
		switch (m) {
		case 0:
			minC = new Circle(new Point2D.Double(-1, -1), 0);
			break;
		case 1:
			minC = new Circle(b[0].toDouble(), 0);
			break;
		case 2:
			minC = new Circle(b[0].add(b[1]).div(2).toDouble(),
					b[0].distance(b[1]) / 2);
			break;
		default:
			return new Circle(b[0], b[1], b[2]);
		}

		// ... Now see if all the points in P are enclosed.
		for (int i = 0; i < n; i++)
			if (p.get(i).distance(minC.position) > minC.radius) {
				// ... Compute B <--- B union P[i].
				b[m] = new Point2(p.get(i));

				// ... Recurse
				minC = minCircle(i, p, m + 1, b);
			}
		return minC;

	}

	/**
	 * Calculates the length of a line between two points, i.e. the distance
	 * between the points
	 * 
	 * @param a
	 *            - the first point
	 * @param b
	 *            - the second point
	 * @return distance between a and b
	 * @deprecated Use a.distance(b) instead
	 */
	public static double lineSize(Point2 a, Point2 b) {
		double s = (double) (b.getX() - a.getX());
		double t = (double) (b.getY() - b.getY());
		return Math.sqrt(s * s + t * t);
	}

	/**
	 * Checks if the given value is within a certain range (epsilon) of the
	 * target value. To be used for pixel thresholding.
	 * 
	 * @param hue
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
	 * @return a LinkedList of Point2's, containing the vertices of the surrounding
	 *         polygon
	 */
	public static LinkedList<Point2> convexHull(ArrayList<Point2> pts) {
		if (pts.size() > 0) {
			Point2 pHull = pts.get(0);
			Point2 endPoint;

			LinkedList<Point2> p = new LinkedList<Point2>();

			for (Point2 pt : pts)
				if (pt.getX() < pHull.getX())
					pHull = pt;

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
	 * @return true if the point is within the polygon, false otherwise
	 */
	public static boolean isInHull(LinkedList<Point2> borderPoints, int x, int y) {
		int i;
		int j;
		boolean result = false;
		for (i = 0, j = borderPoints.size() - 1; i < borderPoints.size(); j = i++) {
			if ((borderPoints.get(i).y > y) != (borderPoints.get(j).y > y)
					&& (x < (borderPoints.get(j).x - borderPoints.get(i).x)
							* (y - borderPoints.get(i).y)
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
	 * @return true if the point is within the polygon, false otherwise
	 */
	public static boolean isInHull(LinkedList<Point2> borderPoints, Point2 p) {
		return isInHull(borderPoints, p.x, p.y);
	}
}
