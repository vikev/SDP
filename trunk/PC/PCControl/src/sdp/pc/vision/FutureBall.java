package sdp.pc.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

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
	 * The minimum velocity of the ball in pixels per second to estimate its
	 * stopping point.
	 */
	private static final int MIN_ESTIMATE_VELOCITY = 10;

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
	 * @param x
	 * @param y
	 */
	public static Intersect collide8(double x, double y, Intersect inter) {
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
		//drawLine(pts[p[0]].add(offsPt), pts[p[1]].sub(offsPt));

		Point2 A = new Point2((int) x, (int) y); //Ball
		Point2 B = pts[p[0]]; // Collision
		double angle = getOutwardAngle(A, B);
		inter.setIntersection(B);
		inter.setAngle(angle);
		return inter;
	}

	/**
	 * Estimates ball stop point given velocity and position of the ball
	 * 
	 * @return predicted ball position
	 */
	public static Intersect estimateRealStopPoint() {
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
	public static Intersect estimateStopPoint(Point2 vel, Point2 ball) {
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
		Intersect inter = new Intersect(ball, Point2.EMPTY, Point2.EMPTY,
				Point2.EMPTY, Double.NaN);
		if (vel.modulus() > MIN_ESTIMATE_VELOCITY) {
			while (collision.equals(Point2.EMPTY) && distToStop > 0) {
				if (!pitchContains(new Point2((int) iteratorX, (int) iteratorY))) {
					collision = new Point2((int) iteratorX, (int) iteratorY);

					inter = collide8(iteratorX, iteratorY, inter);
					Point2 temp = new Point2((int) tarX, (int) tarY);
					Point2 reboundPoint = getReboundPoint(
							inter.getIntersection(), inter.getBall(), 5,
							inter.getAngle());
					inter.setDeflection(reboundPoint);
				}
				iteratorX += vHatX;
				iteratorY += vHatY;
				distToStop -= 1;
			}
		}

		// Return bundle
		return inter;
	}

	/**
	 * Estimates the intersection point of a moving ball, with a robot whose x
	 * co-ordinate remains static.
	 * 
	 * @param movingPos
	 *            - the position of the object (moving object with arbitrary
	 *            facing angle)
	 * @param movingFacing
	 *            - the facing angle of object
	 * @param staticPos
	 *            - the position of the robot whose x co-ordinate should remain
	 *            static
	 * @return estimated position of the robot to intersect the object
	 */
	public static Point2 estimateMatchingYCoord(Point2 movingPos,
			double movingFacing, Point2 staticPos) {

		// Assume the ball is moving very fast, give it a velocity of 1000.
		int x = 1000;
		double angle = movingFacing;

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
		if (movingFacing < 180) {
			y = -y;
		}
		// mirror on y-axis if facing angle is on 1st or 4th quadrant
		if (movingFacing > 270 || movingFacing < 90) {
			x = -x;
		}

		Intersect stopPos = estimateStopPoint(new Point2(x, y), movingPos);

		Point2 intersection = stopPos.getIntersection();
		Point2 estimatedPoint = stopPos.getDeflection();

		if (betweenTwoPoints(staticPos.getX(), intersection.getX(),
				movingPos.getX())) {
			estimatedPoint = intersection;
		}

		double a = (movingPos.getY() - estimatedPoint.getY())
				/ (double) (movingPos.getX() - estimatedPoint.getX());
		double b = movingPos.getY() - a * movingPos.getX();

		double predY = a * staticPos.getX() + b;
		// double deltaY = Math.abs(estimatedPoint.getY() - movingPos.getY())
		// * Math.abs(staticPos.getX() - movingPos.getX())
		// / Math.abs(estimatedPoint.getX() - movingPos.getX());

		// Does nothing
		// double predY;
		// if (movingFacing < 180) {
		// predY = deltaY + movingPos.getY();
		// } else {
		// predY = movingPos.getY() - deltaY;
		// }

		// check if point is within the boundaries and if not return (0,0)
		Point2 target = new Point2(staticPos.getX(), (int) predY);
		if (pitchContains(target)) {
			return target;
		}
		return Point2.EMPTY;
	}

	/**
	 * Returns true if point x is between other two points xStart and xEnd
	 * 
	 * @param x
	 * @param xStart
	 * @param xEnd
	 * @return boolean value
	 */
	private static boolean betweenTwoPoints(int x, int xStart, int xEnd) {
		if (xStart < xEnd) {
			if (xStart <= x && x <= xEnd) {
				return true;
			}
		} else {
			if (xEnd <= x && x <= xStart) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates the angle between three points using arc cos.
	 * 
	 * @param A
	 *            - Location of the ball on the pitch.
	 * @param B
	 *            - Point of collision with boundary.
	 * 
	 * @return angle between point A,B and two closest points on the boundary
	 */
	public static double getOutwardAngle(Point2 A, Point2 B) {
		int[] twoPoints = getCollisionWall(B);
		double outAngle;
		Point2 C = new Point2(twoPoints[0],twoPoints[1]);
		Point2 D = new Point2(twoPoints[2],twoPoints[3]);

		double aC = C.distance(B);
		double aD = D.distance(B);
		double c = A.distance(B);
		double bD = A.distance(D);
		double bC = A.distance(C);
		
		double inAngleC = Math
				.acos((Math.pow(aC, 2) + Math.pow(c, 2) - Math.pow(
						bC, 2)) / (2 * aC * c));
		double inAngleD = Math
				.acos((Math.pow(aD, 2) + Math.pow(c, 2) - Math.pow(
						bD, 2)) / (2 * aD * c));
		if ((inAngleC * 180/Math.PI)>90){
			outAngle = 180 - (inAngleD * 180/Math.PI);
		} else {
			outAngle = 180 - (inAngleC *180/Math.PI);
	
		}
		
		
		return outAngle;
	}
	/**
	 * Calculates the angle between three points using arc cos.
	 * 
	 * @param ball
	 *            - Location of the ball on the pitch.
	 * @param intersection
	 *            - Point of collision with boundary.
	 * @param diatance
	 *            - distance from collision to estimated before rebound
	 * @param angle
	 *            - angle return by getOutwardAngle function
	 *            
	 * @return Expected point after rebound
	 */            
	 
	// could take a distance
	public static Point2 getReboundPoint(Point2 ball, Point2 intersection,
			double distance, double angle) {
		double x = 0;
		double y = 0;
		Point2 top = new Point2(intersection.getX(),0);
		double angleTrue = 0;
		int quad = getQuadrant(ball, intersection,top);
		if(quad==0){
			System.out.println("0 : " +angle);
			angleTrue = 90 - angle;
			x = intersection.getX() + distance * Math.cos(angle);
			y = intersection.getY() - distance * Math.sin(angle);
		}
		else if(quad==1){
			System.out.println("1 : " +angle);
			angleTrue = 270 + angle;
			x = intersection.getX() - distance * Math.cos(angle);
			y = intersection.getY() - distance * Math.sin(angle);
		}
		else if (quad== 2){		
			System.out.println("2 : " +angle);
			//
			x = intersection.getX() - distance * Math.cos(angle);
			y = intersection.getY() + distance * Math.sin(angle);
		} else if(quad==3){
			System.out.println("3 : " +angle);
			x = intersection.getX() + distance * Math.cos(angle);
			y = intersection.getY() + distance * Math.sin(angle);
		}
		Point2 estimation = new Point2((int) x, (int) y);
		return estimation;
	}
	/**
	 * 
	 * @param collide
	 * @return
	 */
	public static int[] getCollisionWall(Point2 collide){
		Pitch pitch = state.getPitch();
		ArrayList<Point2> points = pitch.getArrayListOfPoints();
		double minA = 1000;
		double minB = 1000;
		Point2 wallPointA = new Point2(0,0);
		Point2 wallPointB = new Point2(0,0);
		for (Point2 point : points){
			double distance = collide.distance(point);
			if (distance < minA){
				wallPointA = point;
				minA = distance;
			} 
			else if(distance > minA && distance < minB){
				wallPointB = point;
				minB = distance;
			}
		}
		int[] twoPoints = {wallPointA.getX(),wallPointA.getY(),wallPointB.getX(),wallPointB.getY()};
		return twoPoints;
		
	}
	
	public static int getQuadrant(Point2 A, Point2 B, Point2 C) {
		double a = C.distance(B);
		double b = A.distance(C);
		double c = A.distance(B);
		//System.out.println();
		double angleR = Math
				.acos((Math.pow(a, 2) + Math.pow(c, 2) - Math.pow(
						b, 2)) / (2*a*c));
		
		double angleD = angleR * (180/Math.PI);
		if(angleD > 90){
			if(A.getX()<B.getX()){
				return 2;
			} else {
				return 3;
			}
		} else {
			if(A.getX()<B.getX()){
				return 1;
			} else {
				return 0;
			}
		}
	}

}
