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
	 * The angle TODO: Of the wall that caused the reflection?
	 */
	private double angle;

	/**
	 * Empty constructor
	 */
	public Intersect() {
		this.ball = Point2.EMPTY;
		this.intersection = Point2.EMPTY;
		this.deflection = Point2.EMPTY;
		this.angle = Double.NaN;
	}

	/**
	 * Full constructor
	 * 
	 * @param ball
	 * @param intersection
	 * @param deflection
	 * @param angle
	 */
	public Intersect(Point2 ball, Point2 intersection, Point2 deflection,
			double angle) {
		this.ball = ball;
		this.intersection = intersection;
		this.deflection = deflection;
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
	 * @return
	 */
	public Point2 getResult() {
		if (!this.deflection.equals(Point2.EMPTY)) {
			return this.deflection;
		} else if (!this.intersection.equals(Point2.EMPTY)) {
			return this.intersection;
		}
		return Point2.EMPTY;
	}
}
