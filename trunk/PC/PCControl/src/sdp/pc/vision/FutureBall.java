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
	 * The factor to apply to a moving body after hitting a wall in order to
	 * predict energy lost. A higher COR produces a more elastic collision.
	 */
	private static final double COEFFICIENT_OF_RESTITUTION = 0.4;
	/**
	 * The minimum velocity of the ball in pixels per second to estimate its
	 * stopping point.
	 */
	private static final int MIN_ESTIMATE_VELOCITY = 30;

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
	 * Flag that's used internally. Gets set to true if a goal line is found and
	 * false otherwise.
	 */
	private static boolean goalLine = false;

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
	 *            - the position of the ball in coordinate format
	 * @return predicted position
	 */
	public static Intersect estimateStopPoint(Point2 vel, Point2 ball) {

		// Initialise components used by algorithm
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

		// Compute a normalised del pair with modulus 1.0
		double vHatX = tarX - iteratorX;
		double vHatY = tarY - iteratorY;
		vHatX /= distToStop;
		vHatY /= distToStop;

		// Initialise empty collision point and Intersection
		Intersect inter = new Intersect(ball, ball);

		// Only estimate data if the ball is moving at a reasonable velocity
		if (vel.modulus() > MIN_ESTIMATE_VELOCITY) {

			// Loop until a collision is recognised or the the iterator reaches
			// the initial estimate
			while (distToStop > 0) {

				// Collision found if the point found is off the field
				Point2 iteratorPt = new Point2((int) iteratorX, (int) iteratorY);
				if (!pitchContains(iteratorPt)) {

					// Apply COR
					distToStop *= COEFFICIENT_OF_RESTITUTION;

					inter.addIntersection(iteratorPt);
					double newAng = getDeflectionAngle(ball, iteratorPt)
							* Math.PI / 180.0;
					if (goalLine) {
						distToStop = 0;
					}
					vHatX = Math.cos(newAng);
					vHatY = Math.sin(newAng);
				}

				// Increment the iterator (go to the next pixel on a line
				// between ball and initialEstimate)
				iteratorX += vHatX;
				iteratorY += vHatY;
				distToStop -= 1;
			}
		}

		inter.setEstimate(new Point2((int) iteratorX, (int) iteratorY));

		// Return bundle
		return inter;
	}

	public static Point2 matchingYCoord(Point2 movingPos, double movingFacing,
			Point2 staticPos) {

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
		double diff = distBallToRobot * Math.sin(theta * Math.PI / 180)
				/ Math.sin(theta3 * Math.PI / 180);

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

		Point2 intersection = stopPos.getIntersections().get(0);
		Point2 estimatedPoint = stopPos.getEstimate();

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
	 * method for getting the two points which form the border that collision
	 * point 'collide' lies between. Works by getting the closest border point
	 * and then comparing its angle with the two immediate neighbours of that
	 * one. Such is necessary because getting the two closest points will have
	 * unexpected behaviour since some boundary panels are longer than others.
	 * 
	 * @param collide
	 *            - point of collision with wall
	 * 
	 * @return two points which represent the points that form the boundary.
	 */
	public static Point2[] getCollisionWall(Point2 collide) {

		// Initialise components used by algorithm
		Pitch pitch = state.getPitch();
		Point2[] vals = new Point2[2];

		// Get the closest boundary vertex to collide
		Point2 nearest = pitch.getVertexNearest(collide);
		vals[0] = nearest;

		// Get the two boundary vertices that neighbour 'nearest' as candidates
		Point2[] candidates = pitch.getBoundariesNeighbouring(nearest);

		// Get the angles formed between collision point -> nearest vertex ->
		// candidate. The candidate which produces a smaller angle is the
		// correct one.
		double theta = collide.angleTo(nearest);
		double theta2 = nearest.angleTo(candidates[0]);
		double theta3 = nearest.angleTo(candidates[1]);
		double cand1Ang = Math.abs(Alg.normalizeToUnitDegrees(theta - theta2));
		double cand2Ang = Math.abs(Alg.normalizeToUnitDegrees(theta - theta3));

		// Compare candidate angles and return the correct wall.
		if (cand1Ang < cand2Ang) {
			vals[1] = candidates[0];
		} else {
			vals[1] = candidates[1];
		}
		goalLine = checkForGoalLine(vals);
		return vals;
	}

	/**
	 * Returns whether or not the given point pair is a goalmouth or not.
	 * 
	 * @param vals
	 * @return
	 */
	private static boolean checkForGoalLine(Point2[] vals) {
		int ind1 = state.getPitch().getListIndFromPt(vals[0]);
		int ind2 = state.getPitch().getListIndFromPt(vals[1]);
		if ((ind1 == 6 && ind2 == 7) || (ind1 == 13 && ind2 == 14)
				|| (ind1 == 7 && ind2 == 6) || (ind1 == 14 && ind2 == 13)) {
			return true;
		}
		return false;
	}

	/**
	 * Given a ball point and collision point, return the resultant angle of the
	 * ball were it to reflect off the boundary.
	 * <p />
	 * <b>Sets the static flag goalLine if the border is a goal line, and unsets
	 * otherwise</b>
	 * 
	 * @param ball
	 *            - current ball position
	 * @param collision
	 *            - the point of collision
	 * @return the new angle about the horizontal (the angle with respect to 0)
	 */
	public static double getDeflectionAngle(Point2 ball, Point2 collision) {

		// Get some basic angles to use in calculations
		Point2[] boundaries = getCollisionWall(collision);
		double boundaryAngle = boundaries[0].angleTo(boundaries[1]);
		double boundaryNormal = Alg
				.normalizeToBiDirection(boundaryAngle + 90.0);
		double collisionToBall = Alg.normalizeToBiDirection(collision
				.angleTo(ball));

		// Get the angle between the boundary's normal and the angle from ball
		// to collision
		double diff = Alg.normalizeToBiDirection(boundaryNormal
				- collisionToBall);

		// Calculate one of two possible results
		double resultCandidate = Alg.normalizeToBiDirection(collisionToBall
				+ diff * 2.0);

		// Build an arbitrary point 50 pixels away to see if the candidate works
		// TODO: Bounds checking
		Point2 candChecker = collision.offset(50.0, resultCandidate);
		if (pitchContains(candChecker)) {
			return resultCandidate;
		}

		// Otherwise the result is just the candidate's opposite
		return Alg.normalizeToBiDirection(resultCandidate + 180.0);
	}
}
