package sdp.pc.vision;

/**
 * Class to help return a small bundle of information in the rebounding code
 * 
 */
public class Intersect {
	private static Point2 ball;
	private static Point2 intersection;
	private static double angle;
	
	public static Point2 getBall() {
		return ball;
	}
	public static void setBall(Point2 ball) {
		Intersect.ball = ball;
	}
	public static Point2 getIntersection() {
		return intersection;
	}
	public static void setIntersection(Point2 intersection) {
		Intersect.intersection = intersection;
	}
	public static double getAngle() {
		return angle;
	}
	public static void setAngle(double angle) {
		Intersect.angle = angle;
	}
	

}
