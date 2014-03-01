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
	@SuppressWarnings("unused")
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

//		double angleInDegrees = pts[p[0]].angleTo(pts[p[1]]);
//		double ang = angleInDegrees * Math.PI / 180.0;
//		Point2 offsPt = new Point2((int) (50.0 * Math.cos(ang)),
//				(int) (50.0 * Math.sin(ang)));
		// drawLine(pts[p[0]].add(offsPt), pts[p[1]].sub(offsPt));

		Point2 A = new Point2((int) x, (int) y); // Ball
		Point2 B = pts[p[0]]; // Collision
		// double angle = getOutwardAngle(A, B);

		double[] pointAndAngle = getTrueAngle(A, B);
		Point2 CD = new Point2((int)pointAndAngle[0],(int)pointAndAngle[1]);
		double angle = pointAndAngle[2];
		
	
		inter.setIntersection(B); // Point it meets wall
		inter.setBall(CD);
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
				ball, Double.NaN);
		if (vel.modulus() > MIN_ESTIMATE_VELOCITY) {
			while (collision.equals(Point2.EMPTY) && distToStop > 0) {
				if (!pitchContains(new Point2((int) iteratorX, (int) iteratorY))) {
					collision = new Point2((int) iteratorX, (int) iteratorY);

					
					inter = collide8(iteratorX, iteratorY, inter);
					Point2 temp = new Point2((int) tarX, (int) tarY);
					double distance = temp.distance(ball);
					Point2 rebound = getReboundPoint(inter.getIntersection(),inter.getBall(),distance,inter.getAngle());
					inter.setDeflection(rebound);

				}
				iteratorX += vHatX;
				iteratorY += vHatY;
				distToStop -= 1;
			}
		}

		// Return bundle
		return inter;
	}
	
	public static Point2 matchingYCoord(Point2 movingPos, double movingFacing, Point2 staticPos){
		
		// The ball position, robot position, and desired position form a
		// triangle. Since two angles and one side can be trivially calculated,
		// we can use the law of sines to calculate the diff side length, and
		// therefore the estimated Y coordinate.

		// Get the angle and distance from ball to robot
		double ballToRobot = movingPos.angleTo(staticPos);
		double distBallToRobot = movingPos.distance(staticPos);

		// Calculate all the necessary angles:
		// theta = angle between ball facing and robot
		double theta = Alg.normalizeToBiDirection(movingFacing - ballToRobot);

		// theta2 = angle between ball to robot and the perpendicular
		double theta2 = Alg.normalizeToBiDirection(Math.min(ballToRobot - 90.0,
				ballToRobot - 270.0));

		// theta3 = the third angle (using sum of angles in a triangle)
		double theta3 = 180 - (theta + theta2);

		// Use the law of sines - a/sin(A) = b/sin(B) = c/sin(C)
		// diff / sin(theta) = ballToRobotDist / (theta3) ->
		// diff = ballToRobotDist * sin(theta)/sin(theta3)
		double diff = distBallToRobot * Math.sin(theta*Math.PI/180) / Math.sin(theta3*Math.PI/180);

		// If the angle between the balls facing and the static position is too
		// large, the ball is moving away (return empty point), otherwise,
		// return the static point offset by diff
		if (Math.abs(theta) > 90.0) {
			return Point2.EMPTY;
		} else {
			return new Point2(staticPos.getX(), staticPos.getY() + (int) diff);
		}
		
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
		// double outAngle;
		Point2 C = new Point2(twoPoints[0], twoPoints[1]);
		Point2 D = new Point2(twoPoints[2], twoPoints[3]);
		System.out.println("C: " + C.toString() + ", D: " + D.toString());

		double aC = C.distance(B);
		double aD = D.distance(B);
		double c = A.distance(B);
		double bD = A.distance(D);
		double bC = A.distance(C);

		double inAngleC = Math.acos((Math.pow(aC, 2) + Math.pow(c, 2) - Math
				.pow(bC, 2)) / (2 * aC * c));
		double inAngleD = Math.acos((Math.pow(aD, 2) + Math.pow(c, 2) - Math
				.pow(bD, 2)) / (2 * aD * c));
		double abc = inAngleC * 180 / Math.PI;
		double abd = inAngleD * 180 / Math.PI;
		System.out.println("For Ball at " + A.toString() + " and collison at "
				+ B.toString() + ", the angle for abc is " + abc
				+ " and the angle for abd is " + abd);

		if ((abc) > 90) {
			return abd;

		} else {
			return abc;
		}

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
	public static Point2 getReboundPoint(Point2 intersection, Point2 wallPoint,
			double distance, double angle) {
		double x = 0;
		double y = 0;
		x = intersection.getX() + distance * Math.cos(angle);
		y = intersection.getY() + distance * Math.sin(angle);
		Point2 estimation = new Point2((int) x, (int) y);
		return estimation;
	}

	/**
	 * 
	 * @param collide
	 *            - point of collision with wall
	 * 
	 * @return array representation of the two closest points
	 */
	public static int[] getCollisionWall(Point2 collide) {
		Pitch pitch = state.getPitch();
		ArrayList<Point2> points = pitch.getArrayListOfPoints();
		double minA = 1000;
		Point2 wallPointA = new Point2(0, 0);
		Point2 wallPointB = new Point2(0, 0);
		int min = 0;
		for (Point2 point : points) {
			double distance = collide.distance(point);
			if (distance < minA) {
				min = points.indexOf(point);
				wallPointA = point;
				minA = distance;
			}
		}
		double distanceA;
		double distanceB;

		if (min == 0) {
			Point2 A = points.get(points.size() - 1);
			Point2 B = points.get(min + 1);
			distanceA = collide.distance(A);
			distanceB = collide.distance(B);
			if (distanceA < distanceB) {
				wallPointB = points.get(points.size() - 1);

			} else {
				wallPointB = points.get(min + 1);
			}
		} else if (min != points.size()-1){
			Point2 A = points.get(min - 1);
			Point2 B = points.get(min + 1);
			distanceA = collide.distance(A);
			distanceB = collide.distance(B);
			if (distanceA < distanceB) {
				wallPointB = points.get(min - 1);
			} else {
				wallPointB = points.get(min + 1);
			}

		}

		int[] twoPoints = { wallPointA.getX(), wallPointA.getY(),
				wallPointB.getX(), wallPointB.getY() };
		return twoPoints;

	}

	public static double[] getTrueAngle(Point2 A, Point2 B) {
		int[] twoPoints = getCollisionWall(B);
		Point2 C = new Point2(twoPoints[0], twoPoints[1]);
		Point2 D = new Point2(twoPoints[2], twoPoints[3]);
		double trueAngle = 0;
		double[] returnedList = { 0, 0, 0 };
		// angle to left point of wall
		double aC = C.distance(B);
		double bC = A.distance(C);
		double c = A.distance(B);
		double angleCR = Math.acos((Math.pow(aC, 2) + Math.pow(c, 2) - Math
				.pow(bC, 2)) / (2 * aC * c));
		double angleCD = angleCR * (180 / Math.PI);
		// angle to right point of wall
		double aD = D.distance(B);
		double bD = A.distance(D);
		double angleDR = Math.acos((Math.pow(aD, 2) + Math.pow(c, 2) - Math
				.pow(bD, 2)) / (2 * aD * c));
		double angleDD = angleDR * (180 / Math.PI);
		if (angleDD > 90) {
			trueAngle = angleDD - angleCD;
			returnedList[0] = C.getX();
			returnedList[1] = C.getY();
			returnedList[2] = trueAngle;
		} else {
			trueAngle = angleCD - angleDD;
			returnedList[0] = D.getX();
			returnedList[1] = D.getY();
			returnedList[2] = trueAngle;
		}
		return returnedList;
	}
	/*
	 * Useless code now public static int getQuadrant(Point2 A, Point2 B, Point2
	 * C) { double a = C.distance(B); double b = A.distance(C); double c =
	 * A.distance(B); double angleR = Math.acos((Math.pow(a, 2) + Math.pow(c, 2)
	 * - Math.pow( b, 2)) / (2 * a * c));
	 * 
	 * double angleD = angleR * (180 / Math.PI);
	 * System.out.println("Angle to true north: " + angleD); if (angleD > 90) {
	 * if (A.getX() < B.getX()) { return 2; } else { return 3; } } else { if
	 * (A.getX() < B.getX()) { return 1; } else { return 0; } } }
	 */

}
