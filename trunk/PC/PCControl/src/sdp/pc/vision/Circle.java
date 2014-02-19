package sdp.pc.vision;

import java.awt.geom.Point2D;

/**
 * Our implementation of a Circle which essentially contains a center and
 * radius.
 * 
 */
public class Circle {
	/**
	 * The centre of the circle
	 */
	private Point2D.Double position;
	
	/**
	 * The radius of the circle
	 */
	private double radius;

	/**
	 * Gets the centre of this circle
	 */
	public Point2D.Double getPosition() {
		return position;
	}

	/**
	 * Gets the radius of this circle
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Constructs a new circle given a centre point and a radius
	 * 
	 * @param pos
	 *            the centre of the circle
	 * @param radius
	 *            the radius of the circle
	 */
	public Circle(Point2D.Double pos, double radius) {
		this.position = pos;
		this.radius = radius;
	}

	/**
	 * Constructs a new circle determined by three points lying on its boundary
	 * 
	 * TODO: What does method do if the points don't form a circle?
	 * 
	 * @param a
	 *            the first point lying on the circle's boundary
	 * @param b
	 *            the second point lying on the circle's boundary
	 * @param c
	 *            the third point lying on the circle's boundary
	 */
	public Circle(Point2 a, Point2 b, Point2 c) {
		b = b.subtract(a);
		c = c.subtract(a);
		Point2 lb = b.div(2);
		Point2 lc = c.div(2);

		this.position = a.add(Point2.getLinesIntersection(b, c, lb, lc));
		this.radius = a.distance(position);
	}

	/**
	 * Returns whether the given point is inside this circle
	 * 
	 * @param p
	 *            the point to check
	 * @return true if the point is inside the circle, false otherwise
	 */
	public boolean isPointInside(Point2 p) {
		return p.distance(position) < radius;
	}
}
