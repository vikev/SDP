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

	private static final int EXPAND_RADIUS = 10;

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
	public int PAST_BALL_POSITIONS = 2;

	// used to find them correct robot positions
	// contain [team][robot]

	/**
	 * The amount of points observed for each robot
	 */
	private int[][] robotPtsCount = new int[2][2];

	/**
	 * The sum (later average) of the observed points
	 */
	private Point2[][] robotPos = new Point2[2][2];

	@SuppressWarnings("unchecked")
	// can't create generic arrays
	/**
	 *  The list of points for each robot
	 */
	private ArrayList<Point2>[][] robotPts = new ArrayList[][] {
			new ArrayList[] { new ArrayList<Point2>(), new ArrayList<Point2>(), },
			new ArrayList[] { new ArrayList<Point2>(), new ArrayList<Point2>() } };

	/**
	 * The list of points for ball position
	 */
	private int ballPtsCount;

	// TODO: Docu
	private Point2 ballPos;

	// TODO: Docu
	private LinkedList<Point2> ballPastPos = new LinkedList<Point2>();

	/**
	 * List of points belonging to the green plates
	 */
	private ArrayList<ArrayList<Point2>> greenPlatePoints = new ArrayList<ArrayList<Point2>>();

	/**
	 * Clusters giving the positions of the four green plates The given points
	 * are just for initialisation of the vision
	 */
	Cluster[] clusters = { new Cluster(new Point2(110, 220)),
			new Cluster(new Point2(240, 220)),
			new Cluster(new Point2(390, 220)),
			new Cluster(new Point2(520, 220)) };

	// /**
	// * Here we have a gaussian point filter which smoothes the position of the
	// * ball over 5 frames. The sigma parameter in the gaussian implementation
	// * may be worth changing if the ball position seems to lag too much.
	// */
	// private GaussianPointFilter ballVelocityFilter = new
	// GaussianPointFilter(5);
	//
	// /**
	// * Here we have a gaussian point filter which smoothes the velocity of the
	// * ball over 5 frames. The sigma parameter in the gaussian implementation
	// * may be worth changing if the ball position seems to lag too much.
	// */
	// private GaussianPointFilter ballPositionFilter = new
	// GaussianPointFilter(5);

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
	 * Checks if a point is in a quadrant expanded by EXPANDED_RADIUS pixels
	 * 
	 * @param pos
	 *            - Point2 to check
	 * @param quad
	 *            - quadrant to check (1, 2, 3, or 4)
	 * @return
	 */
	public boolean isInExpandedQuadrant(Point2 pos, int quad) {
		LinkedList<Point2> linked = new LinkedList<Point2>(state.getPitch()
				.getQuadrantVertices(quad));
		return Alg.inMinorHull(linked, -1 * EXPAND_RADIUS, pos);
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
		greenPlatePoints.clear();
		for (int i = 0; i < 4; i++)
			greenPlatePoints.add(new ArrayList<Point2>());
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

			// Check if it's a green plate
			if (Colors.isGreen(cRgb, cHsb)) {
				int quad = state.quadrantFromPoint(p) - 1;
				if (quad >= 0 && quad <= 3)
					greenPlatePoints.get(quad).add(p);
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

				// convert to pixels per second
				avgPastPos = avgPastPos.mult(state.getPaintFps());
				state.setBallVelocity(avgPastPos);
			}

			// add to current position to past ones and trim, if necessary
			ballPastPos.addFirst(ballPos);
			if (ballPastPos.size() > PAST_BALL_POSITIONS)
				ballPastPos.removeLast();

			// Update estimated data
			// Point2 position =
			// ballPositionFilter.apply(state.getBallPosition());
			// Point2 velocity =
			// ballVelocityFilter.apply(state.getBallVelocity());
			Point2 position = state.getBallPosition();
			Point2 velocity = state.getBallVelocity();
			state.setFutureData(FutureBall
					.estimateStopPoint(velocity, position));
		} else {
			// If the ball position doesn't exist, update world state
			// accordingly.
			state.setBallPosition(Point2.EMPTY);
			state.setFutureData(Intersect.EMPTY);
		}

		// Find the green plate clusters
		for (int i = 0; i < 4; i++) {
			if (greenPlatePoints.get(i).size() > 10) {
				clusters[i] = Kmeans.doKmeans(greenPlatePoints.get(i),
						clusters[i].getMean())[0];
			} else
				clusters[i] = new Cluster(new ArrayList<Point2>(), Point2.EMPTY);
		}

		// Loop through teams' robots
		for (int team = 0; team < 2; team++)
			for (int robot = 0; robot < 2; robot++) {

				int ptCount = robotPtsCount[team][robot];

				// Remove team-colour pixels if they're not within a green
				// plate
				Point2 tempPos = new Point2(0, 0);
				ArrayList<Point2> tempPts = new ArrayList<Point2>();
				int tempCount = 0;
				for (Point2 p : robotPts[team][robot]) {
					boolean isPointInPlate = false;
					for (Cluster cluster : clusters) {
						if (p.distance(cluster.getMean()) < Constants.ROBOT_CIRCLE_RADIUS / 2) {
							isPointInPlate = true;
						}
					}
					if (isPointInPlate) {
						tempPos = tempPos.add(p);
						tempPts.add(p);
						tempCount++;
					}
				}
				robotPos[team][robot] = tempPos;
				robotPts[team][robot] = tempPts;
				ptCount = tempCount;

				// Check if we saw that robot enough times
				if (ptCount > 5) {
					// Find the robot's centre (mean)
					Point2 newPos = robotPos[team][robot].div(ptCount);

					// Remove the outliers
					ArrayList<Point2> newPts = Point2.removeOutliers(
							robotPts[team][robot], newPos);

					newPos.filterPoints(newPts); // and find it again

					// Now find its facing
					double newFacing = findOrientation(newPos, cRgbs, cHsbs);

					// And update the world state
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
	 *            the RGB colours of the image
	 * @param cHsbs
	 *            the HSB colours of the image
	 * @return the angle the robot is facing in degrees in range [0,360] where
	 *         clockwise is the positive direction
	 */
	private double findOrientation(Point2 robotCentroid, Color[][] cRgbs,
			float[][][] cHsbs) {

		int minX = robotCentroid.getX() - SQUARE_SIZE / 2, maxX = robotCentroid
				.getX() + SQUARE_SIZE / 2, minY = robotCentroid.getY()
				- SQUARE_SIZE / 2, maxY = robotCentroid.getY() + SQUARE_SIZE
				/ 2;

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
		if (correctAngle < 0)
			correctAngle += 360;
		assert (0 <= correctAngle && correctAngle < 360);
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
