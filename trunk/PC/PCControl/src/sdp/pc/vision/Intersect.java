package sdp.pc.vision;

import java.util.ArrayList;

/**
 * Class for abstracting the data for a collision.
 */
public class Intersect {

	/**
	 * A blank Intersect, for comparisons
	 */
	public static final Intersect EMPTY = new Intersect();

	/**
	 * The real location of the ball
	 */
	private Point2 ball;

	/**
	 * The location of the any estimated intersections (empty otherwise)
	 */
	private ArrayList<Point2> intersections = new ArrayList<Point2>();

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
	 * Setter method for the intersections
	 * 
	 * @param intersections
	 */
	public void setIntersections(ArrayList<Point2> intersections) {
		this.intersections = intersections;
	}

	/**
	 * Adds an intersection point to the intersections list
	 * 
	 * @param intersect
	 */
	public void addIntersection(Point2 intersect) {
		this.intersections.add(intersect);
	}

	/**
	 * Getter method for the initial estimate point
	 * 
	 * @return
	 */
	public Point2 getEstimate() {
		return this.estimate;
	}

	/**
	 * Setter method for the initial estimate point
	 */
	public void setEstimate(Point2 pt) {
		this.estimate = pt;
	}
}
