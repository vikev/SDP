package sdp.pc.vision;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Class for estimating the real trajectories of the ball. Feeds data it
 * estimates to the world state. FutureBall is static and monolithic and
 * shouldn't be instantiated.
 * 
 * @author s1143704
 * 
 */
public class FutureBall {

	/**
	 * The estimated fraction of the ball velocity lost per second
	 */
	private static final double ESTIMATED_BALL_FRICTION = 0.6;

	/**
	 * The world state attached to FutureBall.
	 */
	public static WorldState state = Vision.state;

	/**
	 * The estimated collision point. For now, there is at most one collision.
	 */
	public static Point2 collision = Point2.EMPTY;

	/**
	 * Returns true if the pitch contains point q. As long as the isWhite method
	 * is calibrated for your pitch (the convex hull is working properly), it
	 * will work!
	 * 
	 * @param q
	 * @return
	 */
	public static boolean pitchContains(Point2 q) {
		if (Vision.stateListener.pointInPitch(q)) {
			return true;
		}
		return false;
	}

	/**
	 * Draw a line to the world state (you naughty boy) between a and b
	 * 
	 * @param a
	 *            - One Point2
	 * @param b
	 *            - The other Point2
	 */
	private static void drawLine(Point2 a, Point2 b) {
		// Attempt to draw collision boundary
		Graphics g = Vision.frameLabel.getGraphics();
		g.setColor(Color.RED);
		g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
	}

	/**
	 * Calculate if the pitch contains the 8 surrounding pixels around (x,y) and
	 * therefore determine the deflection angle.
	 * 
	 * TODO: Incomplete and untested
	 * 
	 * @param x
	 * @param y
	 */
	public static Intersect collide8(double x, double y) {
		boolean[] q = new boolean[8];
		Point2[] pts = new Point2[8];

		pts[0] = new Point2((int) x + 1, (int) y);
		q[0] = pitchContains(pts[0]);

		pts[1] = new Point2((int) x + 1, (int) y - 1);
		q[1] = pitchContains(pts[1]);

		pts[2] = new Point2((int) x, (int) y - 1);
		q[2] = pitchContains(pts[2]);

		pts[3] = new Point2((int) x - 1, (int) y - 1);
		q[3] = pitchContains(pts[3]);

		pts[4] = new Point2((int) x - 1, (int) y);
		q[4] = pitchContains(pts[4]);

		pts[5] = new Point2((int) x - 1, (int) y + 1);
		q[5] = pitchContains(pts[5]);

		pts[6] = new Point2((int) x, (int) y + 1);
		q[6] = pitchContains(pts[6]);

		pts[7] = new Point2((int) x + 1, (int) y + 1);
		q[7] = pitchContains(pts[7]);

		boolean here = q[0];
		int[] p = new int[2];
		int found = -1;
		boolean b = true;
		for (int i = 1; i < 8; i++) {
			if (!here == q[i]) {
				here = !here;
				if (found > -1) {
					p[found] = i;
				}
				found++;
				if (found > 1) {
					break;
				}
				if (i == 7 && b) {
					i = 0;
					b = false;
				}
			}
		}

		double angleInDegrees = pts[p[0]].angleTo(pts[p[1]]);
		double ang = angleInDegrees * Math.PI / 180.0;
		Point2 offsPt = new Point2((int) (50.0 * Math.cos(ang)),
				(int) (50.0 * Math.sin(ang)));
		drawLine(pts[p[0]].add(offsPt), pts[p[1]].sub(offsPt));

		Point2 A = new Point2((int) x, (int) y); // right most
		Point2 B = pts[p[1]].sub(offsPt); // left most
		Point2 C = pts[p[0]]; // centre

		double angle = getOutwardAngle(A, B, C);
		Intersect intersect = new Intersect();
		intersect.setBall(A);
		intersect.setIntersection(C);
		intersect.setAngle(angle);
		return intersect;

	}

	/**
	 * Estimates ball stop point given velocity and position of the ball
	 * 
	 * @return predicted ball position
	 */
	public static Point2 estimateRealStopPoint() {
		Point2 vel = state.getBallVelocity();
		Point2 pos = state.getBallPosition().copy();
		return estimateStopPoint(vel, pos);
	}

	/**
	 * Estimates object stop point given velocity and position of the object
	 * 
	 * @param vel
	 *            - the velocity of the ball in vector format
	 * @param ball
	 *            - the position of the ball in co-ordinate format
	 * @return predicted position
	 */
	public static Point2 estimateStopPoint(Point2 vel, Point2 ball) {
		double delX = vel.getX(), delY = vel.getY();
		double tarX = ball.getX(), tarY = ball.getY();
		// How much friction to apply to the ball

		double frameFriction = 1.0 - ESTIMATED_BALL_FRICTION;

		// Apply geometric series
		frameFriction = frameFriction / (1 - frameFriction);
		tarX -= delX * frameFriction;
		tarY -= delY * frameFriction;

		// Search for collision
		double iteratorX = ball.getX(), iteratorY = ball.getY();
		double distToStop = (new Point2((int) (tarX - iteratorX),
				(int) (tarY - iteratorY)).modulus());

		double vHatX = tarX - iteratorX;
		double vHatY = tarY - iteratorY;
		vHatX /= distToStop;
		vHatY /= distToStop;
		collision = Point2.EMPTY;
		if (vel.modulus() > 5) {
			while (collision.equals(Point2.EMPTY) && distToStop > 0) {
				if (!pitchContains(new Point2((int) iteratorX, (int) iteratorY))) {
					collision = new Point2((int) iteratorX, (int) iteratorY);

					// collide8(iteratorX, iteratorY);
					Intersect collide = collide8(iteratorX, iteratorY);
					Point2 temp = new Point2((int) tarX, (int) tarY);
					Point2 reboundPoint = getReboundPoint(
							collide.getIntersection(), collide.getBall(), temp,
							collide.getAngle());

				}
				iteratorX += vHatX;
				iteratorY += vHatY;
				distToStop -= 1;
			}
		}

		// Return stop point
		return new Point2((int) tarX, (int) tarY);
	}

	/**
	 * Estimates the intersection point of a moving ball, with a robot whose x
	 * co-ordinate remains static.
	 * 
	 * @param ball
	 *            - the position of the ball (moving object with arbitrary
	 *            facing angle)
	 * @param ballFacing
	 *            - the facing angle of ball
	 * @param staticPos
	 *            - the position of the robot whose x co-ordinate should remain
	 *            static
	 * @return estimated position of the robot to intersect the ball
	 */
	public static Point2 estimateMatchingYCoord(Point2 ball, double ballFacing,
			Point2 staticPos) {

		// Assume the ball is moving very fast, give it a velocity of 1000.
		int x = 1000;
		double angle = ballFacing;

		// Do Lukas-style maths, lose brownie points

		// Note 90 degrees is south due to inverted co-ordinates.
		// Change quadrant 3 to quadrant 1: (mirror on y=-x)
		if (90 < angle && angle < 180) {
			angle = 180 - angle;

			// Change quadrant 2 to quadrant 4: (mirror on y=x)
		} else if (180 < angle && angle < 270) {
			angle -= 180;

			// Change quadrant 1 to quadrant 4 (mirror on x-axis)
		} else if (angle > 270) {
			angle = 360 - angle;
		}

		// Projection: y <-- big number * tan(angle)
		// small angle -> 0, large angle -> inf
		int y = (int) (x * Math.tan(angle * Math.PI / 180));

		// assert y on first two quadrants
		if (ballFacing < 180) {
			y = -y;
		}
		// mirror on y-axis if facing angle is on 1st or 4th quadrant
		if (ballFacing > 270 || ballFacing < 90) {
			x = -x;
		}

		// TODO: I refuse to believe this works
		Point2 stopPos = FutureBall.estimateStopPoint(new Point2(x, y), ball);

		// What the fuck does this do? Nothing
		double deltaY = Math.abs(stopPos.getY() - ball.getY())
				* Math.abs(staticPos.getX() - ball.getX())
				/ Math.abs(stopPos.getX() - ball.getX());

		// Does nothing
		double predY;
		if (ballFacing < 180) {
			predY = deltaY + ball.getY();
		} else {
			predY = ball.getY() - deltaY;
		}
		// check if point is within the boundaries and if not return (0,0)
		Point2 target = new Point2(staticPos.getX(), (int) predY);
		if (pitchContains(target)) {
			return target;
		}
		return Point2.EMPTY;
	}

	/**
	 * Calculates the angle between three points using arc cos.
	 * 
	 * @param A
	 *            - Location of the ball on the pitch.
	 * @param B
	 *            - Point of collision with boundary.
	 * @param C
	 *            - Leftmost point of the drawn line indicating the collision
	 *            wall.
	 * @return angle between point A,B and C
	 */
	public static double getOutwardAngle(Point2 A, Point2 B, Point2 C) {

		double smallA = Math.sqrt(Math.pow((C.getX() - B.getX()), 2)
				+ Math.pow((C.getY() - B.getY()), 2));
		double smallB = Math.sqrt(Math.pow((A.getX() - C.getX()), 2)
				+ Math.pow((A.getY() - C.getY()), 2));
		double smallC = Math.sqrt(Math.pow((A.getX() - B.getX()), 2)
				+ Math.pow((A.getY() - B.getY()), 2));
		// double beforeAcos= ((Math.pow(smallA,2) + Math.pow(smallB,2) -
		// Math.pow(smallC,2))/(2*smallA*smallB));
		double inAngle = Math
				.acos((Math.pow(smallA, 2) + Math.pow(smallB, 2) - Math.pow(
						smallC, 2)) / (2 * smallA * smallB));
		double outAngle = 180 - (inAngle * 180 / Math.PI);
		return outAngle;
	}

	private static Point2 getReboundPoint(Point2 intersection, Point2 ball,
			Point2 temp, double angle) {
		double distance = Math.sqrt(Math.pow(
				(temp.getX() - intersection.getX()), 2)
				+ Math.pow((temp.getY() - intersection.getY()), 2));
        double x = intersection.getX() + distance * Math.cos(angle);
        double y = intersection.getY() + distance * Math.sin(angle);
        Point2 estimation = new Point2((int) x,(int) y);
        //System.out.println("Estimation after rebound:" + estimation.toString());
        drawLine(intersection, estimation);
        return estimation;
	}

}
