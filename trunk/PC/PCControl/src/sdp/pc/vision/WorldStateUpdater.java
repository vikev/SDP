package sdp.pc.vision;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

import sdp.pc.common.Constants;

/**
 * Contains the code used to process (get RGB/HSB values) the current frame;
 * also the actual code processing the frame and updating the underlying world
 * state
 * <p />
 * Take care of feeding it with enough fresh images!
 * 
 * @author s1141301
 */
public class WorldStateUpdater extends WorldStateListener {

	/**
	 * The length of one side of the square bound around robot means for finding
	 * orientation
	 */
	private static final int SQUARE_SIZE = 50;

	/**
	 * The minimum points needed to trigger a robot position update
	 */
	public int MINIMUM_ROBOT_POINTS = 10;

	/**
	 * The minimum points needed to trigger a ball position update
	 */
	public int MINIMUM_BALL_POINTS = 0;

	/**
	 * The amount of past positions used to interpolate the speed of the ball
	 */
	public int PAST_BALL_POSITIONS = 10;

	// used to find them correct robot positions
	// contain [team][robot]

	/**
	 * the amount of points observed for each robot
	 */
	private int[][] robotPtsCount = new int[2][2];

	/**
	 * the sum (later average) of the observed points
	 */
	private Point2[][] robotPos = new Point2[2][2];

	@SuppressWarnings("unchecked")
	// can't create generic arrays
	/**
	 *  the list of points for each robot
	 */
	private ArrayList<Point2>[][] robotPts = new ArrayList[][] {
			new ArrayList[] { new ArrayList<Point2>(), new ArrayList<Point2>(), },
			new ArrayList[] { new ArrayList<Point2>(), new ArrayList<Point2>() } };

	/**
	 * the list of points for ball position
	 */
	private int ballPtsCount;
	Point2 ballPos;
	LinkedList<Point2> ballPastPos = new LinkedList<Point2>();

	/**
	 * Constructs a new WorldStateUpdater to look for new frames, as refreshed
	 * by setCurrentFrame(). When a new frame is detected it first grabs the
	 * frame data using processImage() and then uses updateWorld() to interpret
	 * it and update the specified {@link WorldState}
	 * 
	 * @param targetFps
	 *            the maximum FPS
	 * @param state
	 *            the WorldState to be updated
	 */
	public WorldStateUpdater(int targetFps, WorldState state) {
		super(targetFps, state);
	}

	/**
	 * Does an update to the underlying {@link WorldState} trying to fetch fresh
	 * values for the ball position and velocity, and the robots' position and
	 * facing.
	 */
	@Override
	public void updateWorld(Color[][] cRgbs, float[][][] cHsbs) {

		// reset ball/robot averages and collected points
		ballPtsCount = 0;
		ballPos = new Point2();
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 2; j++) {
				robotPtsCount[i][j] = 0;
				robotPts[i][j].clear();
				robotPos[i][j] = new Point2();
			}

		// the colour data of the 'current' pixel
		Color cRgb;
		float[] cHsb;
		int x, y;

		// whether this point is on the right or left side of the pitch
		int robotSide;

		// Loop through all table values, recognising pixel regions as necessary
		for (Point2 p : pitchPoints) {

			// store some often used values
			x = p.getX();
			y = p.getY();
			cRgb = cRgbs[x][y];
			cHsb = cHsbs[x][y];

			// check if it's a ball colour
			if (Colors.isBall(cRgb, cHsb)) {
				// update its counters
				ballPos = ballPos.add(p);
				ballPtsCount++;
			}

			// check if it's a team colour
			for (int team = 0; team < 2; team++) {
				if (Colors.isTeamColor(team, cRgb, cHsb)) {

					// get the appropriate robot
					if (x < Constants.TABLE_CENTRE_X)
						robotSide = Constants.ROBOT_LEFT;
					else
						robotSide = Constants.ROBOT_RIGHT;

					// update its counters
					robotPos[team][robotSide] = robotPos[team][robotSide]
							.add(p);
					robotPtsCount[team][robotSide]++;
					robotPts[team][robotSide].add(p);
				}

			}
		}

		// update ball position, if any
		if (ballPtsCount > MINIMUM_BALL_POINTS) {
			// get mean current position
			ballPos = ballPos.div(ballPtsCount);
			state.setBallPosition(ballPos);

			// get mean past position (if any)
			if (ballPastPos.size() > 0) {
				Point2 avgPastPos = Point2.EMPTY;
				for (Point2 p : ballPastPos)
					avgPastPos = avgPastPos.add(p);
				avgPastPos = avgPastPos.div(ballPastPos.size());

				// make it an offset from current position
				avgPastPos = avgPastPos.subtract(ballPos);
				state.setBallVelocity(avgPastPos);
			}

			// add to current position to past ones and trim, if necessary
			ballPastPos.addFirst(ballPos);
			if (ballPastPos.size() > PAST_BALL_POSITIONS)
				ballPastPos.removeLast();
			
			// Update estimated stop/collide points
			state.setEstimatedStopPoint(FutureBall.estimateRealStopPoint());
			state.setEstimatedCollisionPoint(FutureBall.collision);
		} else {
			// If the ball position doesn't exist, update world state
			// accordingly.
			state.setBallPosition(Point2.EMPTY);
			state.setEstimatedStopPoint(Point2.EMPTY);
			state.setEstimatedCollisionPoint(Point2.EMPTY);
		}

		// loop through teams' robots
		for (int team = 0; team < 2; team++)
			for (int robot = 0; robot < 2; robot++) {
				// check if we saw that robot enough times
				int ptCount = robotPtsCount[team][robot];
				if (ptCount > MINIMUM_ROBOT_POINTS) {
					// if so, find the centre (mean)
					Point2 newPos = robotPos[team][robot].div(ptCount);

					// remove the outliers
					ArrayList<Point2> newPts = Point2.removeOutliers(
							robotPts[team][robot], newPos);

					newPos.filterPoints(newPts); // and find it again

					// now find its facing
					double newFacing = findOrientation(newPos, cRgbs, cHsbs);

					// and update the world state
					state.setRobotPosition(team, robot, newPos);
					state.setRobotFacing(team, robot, newFacing);
				} else {
					state.setRobotPosition(team, robot, Point2.EMPTY);
					state.setRobotFacing(team, robot, Double.NaN);
				}
			}
	}

	/**
	 * Finds the orientation a robot is facing given its centroid. Works by
	 * first finding all green pixels and calculating their convex hull. Then
	 * finds all black pixels inside that hull and calculates their mean.
	 * Finally returns the angle between the centroid and the black-point-mean
	 * 
	 * @author s1143704, s1141301
	 * @param robotCentroid
	 *            the centroid of the robot
	 * @param cRgbs
	 *            the RGB colors of the image
	 * @param cHsbs
	 *            the HSB colors of the image
	 * @return the angle the robot is facing in degrees in range [0,360] where
	 * 		   clockwise is the positive direction
	 */
	private double findOrientation(Point2 robotCentroid, Color[][] cRgbs, float[][][] cHsbs) {

		int minX = robotCentroid.getX() - SQUARE_SIZE / 2, 
			maxX = robotCentroid.getX() + SQUARE_SIZE / 2, 
			minY = robotCentroid.getY() - SQUARE_SIZE / 2, 
			maxY = robotCentroid.getY() + SQUARE_SIZE / 2;

		// Find all green pixels
		ArrayList<Point2> greenPoints = new ArrayList<Point2>();
		for (int ix = minX; ix < maxX; ix++)
			for (int iy = minY; iy < maxY; iy++) {
				Point2 ip = new Point2(ix, iy);
				if (pointInPitch(ip)) {
					if (Colors.isGreen(cRgbs[ix][iy], cHsbs[ix][iy])) {
						greenPoints.add(ip);
					}
				}
			}
		if (greenPoints.isEmpty()) {
			return 0; // no green pts :(
		}

		// Calculate the hull of the green points
		LinkedList<Point2> hull = Alg.convexHull(greenPoints);

		// Now search for black points
		Point2 blackPos = new Point2();
		int blackCount = 0;
		for (int ix = minX; ix < maxX; ix++)
			for (int iy = minY; iy < maxY; iy++) {
				Point2 ip = new Point2(ix, iy);
				if (Colors.isBlack(cRgbs[ix][iy], cHsbs[ix][iy]))
					if (Alg.isInHull(hull, ip)) {
						blackCount++;
						blackPos = blackPos.add(ip);
					}
			}

		if (blackCount == 0) {
			return 0; // no black pts :(
		}
		
		blackPos = blackPos.div(blackCount);
		double correctAngle = blackPos.angleTo(robotCentroid);
		if (correctAngle < 0) correctAngle += 360;
		assert(0 <= correctAngle && correctAngle < 360);
		return correctAngle;
	}

	/**
	 * An implementation of processImage which updates the given arrays' values
	 * with the corresponding normalised colour values from the given image
	 */
	@Override
	protected void processImage(BufferedImage img, Color[][] cRgbs,
			float[][][] cHsbs) {
		Color cRgb;
		float[] cHsb;
		for (Point2 p : pitchPoints) {

			// get RGB handle
			cRgb = new Color(img.getRGB(p.x, p.y));

			// get HSB handle
			cHsb = cHsbs[p.x][p.y];
			Color.RGBtoHSB(cRgb.getRed(), cRgb.getGreen(), cRgb.getBlue(), cHsb);

			// scale HSB
			float br = cHsb[2];

			// minMaxBrightness is of the form {min, max}
			br = (br - minMaxBrightness[0])
					/ (minMaxBrightness[1] - minMaxBrightness[0]);
			br = Math.min(1, Math.max(0, br));

			cHsb[2] = br;

			// save RGB
			cRgbs[p.x][p.y] = new Color(Color.HSBtoRGB(cHsb[0], cHsb[1],
					cHsb[2]));
			// HSB is up to date

		}
	}
}
