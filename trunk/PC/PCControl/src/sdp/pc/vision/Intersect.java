package sdp.pc.vision;

import java.util.ArrayList;

/**
 * Class for abstracting the data for a collision.
 */
public class Intersect {

	/**
	 * A blank Intersect, for comparisons
	 */
	public static Intersect EMPTY = new Intersect();

	/**
	 * The real location of the ball
	 */
	private Point2 ball;

	/**
	 * The location of the any estimated intersections (empty otherwise)
	 */
	private ArrayList<Point2> intersections = new ArrayList<Point2>();
	
	/**
	 * The time needed by the ball to reach each intersection. 
	 */
	private ArrayList<Double> timings = new ArrayList<Double>();
	
	/**
	 * The speed of the ball when it reaches each intersection. 
	 */
	private ArrayList<Double> speeds = new ArrayList<Double>();

	/**
	 * The location of the estimated stopping point
	 */
	private Point2 estimate;


	/**
	 * Empty constructor
	 */
	public Intersect() {
		this.ball = Point2.EMPTY;
		this.estimate = Point2.EMPTY;
	}

	/**
	 * Full-fledged constructor
	 * 
	 * @param ball
	 * @param est
	 */
	public Intersect(Point2 ball, Point2 est) {
		this.ball = ball;
		this.estimate = est;
	}

	/**
	 * Method for comparing two Intersects
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Intersect))
			return false;
		Intersect i = (Intersect) o;
		return i.getBall() == ball && i.getIntersections() == intersections;
	}

	/**
	 * Getter method for the ball
	 * 
	 * @return
	 */
	public Point2 getBall() {
		return this.ball;
	}

	/**
	 * Setter method for the ball
	 * 
	 * @param ball
	 */
	public void setBall(Point2 ball) {
		this.ball = ball;
	}

	/**
	 * Getter method for the intersections
	 * 
	 * @return
	 */
	public ArrayList<Point2> getIntersections() {
		return this.intersections;
	}

	/**
	 * Gets the intersection timings
	 */
	public ArrayList<Double> getTimings() {
		return this.timings;
	}

	/**
	 * Adds an intersection point to the intersections list
	 * 
	 * @param intersect the point of the intersection
	 * @param timestamp the time of the intersection
	 */
	public void addIntersection(Point2 intersect, double timestamp, double speed) {
		this.intersections.add(intersect);
		timings.add(timestamp);
		speeds.add(speed);
	}

	/**
	 * Getter method for estimated ball result.
	 * 
	 * @return
	 */
	public Point2 getEstimate() {
		return this.estimate;
	}

	/**
	 * Auxiliary method used to check if a given x-value forms a boundary
	 * between two points.
	 * 
	 * @param x
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean xBetween(int x, Point2 a, Point2 b) {
		int min = Math.min(a.x, b.x);
		int max = Math.max(a.x, b.x);
		return (min <= x && x <= max);
	}

	/**
	 * Auxiliary method for finding the intersection point formed by two similar
	 * triangles, for making the robot move along an axis to block the ball.
	 * 
	 * @param x
	 * @param a
	 * @param b
	 * @return
	 */
	public static Point2 xIntersect(int x, Point2 a, Point2 b) {

		// Use the law of similar triangles to find the y difference
		double theta = Alg.normalizeToUnitDegrees(a.angleTo(b));
		double xdiff = x - a.x;
		theta = theta * Math.PI / 180.0;

		// Round the y value to the nearest integer and return
		return new Point2(x, (int) (Math.round(a.y + xdiff * Math.tan(theta))));
	}

	/**
	 * Given an x coordinate, where does this intersection data first cross that
	 * boundary?
	 * 
	 * @param x
	 * @return
	 */
	public Point2 getEstimateIntersectX(int x) {

		// Short circuit the whole method if the ball is unknown!
		if (ball.equals(Point2.EMPTY)) {
			return Point2.EMPTY;
		}

		// First check the ball with respect to the first intersection if it
		// exists
		if (intersections.size() > 0) {
			if (xBetween(x, ball, intersections.get(0))) {
				return xIntersect(x, ball, intersections.get(0));
			}

			// Then check all the intersection points
			for (int q = 0; q < intersections.size() - 1; q++) {
				if (xBetween(x, intersections.get(q), intersections.get(q + 1))) {
					return xIntersect(x, intersections.get(q),
							intersections.get(q + 1));
				}
			}

			// Lastly, check the last intersection point compared to the final
			// estimate
			if (xBetween(x, intersections.get(intersections.size() - 1),
					estimate)) {
				return xIntersect(x,
						intersections.get(intersections.size() - 1), estimate);
			}
		} else {

			if (xBetween(x, ball, estimate)) {
				return xIntersect(x, ball, estimate);
			}
		}

		// If that also doesn't work, the ball prediction never crosses the
		// robot, so just return the predicted y (old behaviour)
		return new Point2(x, estimate.y);
	}

	/**
	 * Setter method for the initial estimate point
	 */
	public void setEstimate(Point2 pt) {
		this.estimate = pt;
	}

	@Override
	public String toString() {
		String q = new String();
		q = q.concat("\nIntersect Data:\n");
		q = q.concat("Ball at: " + ball.toString() + "\n");
		q = q.concat("Estimate at: " + estimate.toString() + "\n");
		if (intersections.size() > 0) {
			q = q.concat(intersections.size() + " intersections at: [");
			for (Point2 p : intersections) {
				q = q.concat(p.toString() + " ");
			}
			q = q.concat("]\n");
		} else {
			q = q.concat("(No Intersections)\n");
		}
		return q;
	}
}
