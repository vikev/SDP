package sdp.pc.vision;

/**
 * Class for abstracting the data for a collision
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
	 * The location of the (first) estimated intersection (empty otherwise)
	 */
	private Point2 intersection;

	/**
	 * The location of the reflected ball if it exists
	 */
	private Point2 deflection;

	/**
	 * The location of the initial estimated stopping point
	 */
	private Point2 initialEstimate;

	/**
	 * The rebound angle of the wall that caused the reflection
	 * 
	 * TODO: Which angle? How to distinguish if the acute angle comes from the
	 * wall or from the wall's normal?
	 */
	private double angle;

	/**
	 * Empty constructor
	 */
	public Intersect() {
		this.ball = Point2.EMPTY;
		this.intersection = Point2.EMPTY;
		this.deflection = Point2.EMPTY;
		this.initialEstimate = Point2.EMPTY;
		this.angle = Double.NaN;
	}

	/**
	 * Full constructor
	 * 
	 * @param ball
	 *            - real location of the ball
	 * @param intersection
	 *            - the point the ball would hit a boundary, if it exists
	 * @param deflection
	 *            - the point after the ball hits a boundary, if it exists
	 * @param initialEstimate
	 *            - the initial estimated stopping point of the ball
	 * @param angle
	 *            - the rebound angle after a collision
	 */
	public Intersect(Point2 ball, Point2 intersection, Point2 deflection,
			Point2 initialEstimate, double angle) {
		this.ball = ball;
		this.intersection = intersection;
		this.deflection = deflection;
		this.initialEstimate = initialEstimate;
		this.angle = angle;
	}

	/**
	 * Method for comparing two Intersects
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Intersect))
			return false;
		Intersect i = (Intersect) o;
		return i.getAngle() == angle && i.getBall() == ball
				&& i.getDeflection() == deflection
				&& i.getIntersection() == intersection;
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
	 * Getter method for the intersection
	 * 
	 * @return
	 */
	public Point2 getIntersection() {
		return this.intersection;
	}

	/**
	 * Setter method for the intersection
	 * 
	 * @param intersection
	 */
	public void setIntersection(Point2 intersection) {
		this.intersection = intersection;
	}

	/**
	 * Getter method for the initial estimate point
	 * 
	 * @return
	 */
	public Point2 getInitialEstimate() {
		return this.initialEstimate;
	}

	/**
	 * Setter method for the initial estimate point
	 */
	public void setInitialEstimate(Point2 pt) {
		this.initialEstimate = pt;
	}

	/**
	 * Getter method for the TODO: Deflection? angle
	 * 
	 * @return angle in TODO: degrees?
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * Setter method for the TODO: Deflection? angle in TODO: degrees?
	 * 
	 * @param angle
	 */
	public void setAngle(double angle) {
		this.angle = angle;
	}

	/**
	 * Setter method for the deflection point
	 * 
	 * @param pt
	 */
	public void setDeflection(Point2 pt) {
		this.deflection = pt;
	}

	/**
	 * Getter method for the deflection point
	 * 
	 * @return
	 */
	public Point2 getDeflection() {
		return this.deflection;
	}

	/**
	 * Method for getting the best result of an intersection - looks for the
	 * final estimated ball position
	 * 
	 * TODO: Deflection disabled while it doesn't work
	 * 
	 * @return
	 */
	public Point2 getResult() {
		// if (!this.deflection.equals(Point2.EMPTY)) {
		// return this.deflection;
		// } else if (!this.intersection.equals(Point2.EMPTY)) {
		if (!this.intersection.equals(Point2.EMPTY)) {
			return this.intersection;
		} else if (!this.initialEstimate.equals(Point2.EMPTY)) {
			return this.initialEstimate;
		}
		return Point2.EMPTY;
	}
}
