package sdp.pc.vision;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Contains information about the Pitch dimensions and the zones a robot is
 * supposed to be in. Currently only checks X values; works well given that
 * Colors.isWhite() returns proper values
 * 
 * TODO: add Y values checks TODO: detect goal positions automatically (would it
 * be reliable enough?)
 * 
 * @author s1141301
 * 
 */
public class Pitch {

	private static final int PITCH_SIDE_Y_NPOINTS = 300;

	private static final int PITCH_ZONE_X_NPOINTS = 195;

	private static final int PITCH_SIDE_X_NPOINTS = 99;

	private static final int SOME_SHANO_VALUE = 4;

	private static final int Y_END = 360, Y_BEGIN = 85;
	private static final int X_END = 570, X_BEGIN = 310;

	/**
	 * The radius of a robot TODO: figure out a value for this
	 */
	private int robotRadius = 0;

	/**
	 * The x coordinates of the goal lines
	 */
	int[] goalLineX = new int[2];

	/**
	 * The y coordinates of the goal lines
	 */
	int[] goalLineY = new int[2];

	/**
	 * The x coordinates of the (middle of the) zones
	 */
	int[] zoneX = new int[3];
	/**
	 * The y coordinates of the pitches
	 */
	int[] pitchY = new int[2];

	int[] pitchCornerX = new int[2];

	boolean initialized = false;

	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Gets whether the specified point is in our defender zone. Uses
	 * Vision.state.getDirection() to determine our team.
	 * 
	 * @param p
	 *            the point to check
	 */
	public boolean isPointInDefenderZone(Point2 p) {
		if (!robotInBounds(p.y, pitchY[0], pitchY[1]))
			return false;

		if (Vision.state.getDirection() == 1) // shoot right => our is left!
			return robotInBounds(p.x, goalLineX[0], zoneX[0]);
		else
			return robotInBounds(p.x, zoneX[2], goalLineX[1]);
	}

	/**
	 * Checks whether the specified value is in the range [min + robotRadius;
	 * max - robotRadius]
	 */
	private boolean robotInBounds(int val, int min, int max) {
		return val >= min + robotRadius && val <= max - robotRadius;
	}

	/**
	 * Gets whether the specified point is in our attacker zone Uses
	 * Vision.state.getDirection() to determine our team.
	 * 
	 * @param p
	 *            the point to check
	 */
	public boolean isPointInAttackerZone(Point2 p) {
		if (!robotInBounds(p.y, pitchY[0], pitchY[1]))
			return false;

		if (Vision.state.getDirection() == 1) // shoot right
			return robotInBounds(p.x, zoneX[1], zoneX[2]);
		else
			return robotInBounds(p.x, zoneX[0], zoneX[1]);
	}

	public Pitch() {
	}

	private Point2 horizontalSwipe(Color[][] rgb, float[][][] hsb, int y) {
		Point2 p = new Point2(0, Vision.WIDTH - 1);

		while (!Colors.isWhite(rgb[p.x][y], hsb[p.x][y]) && p.x < Vision.WIDTH)
			p.x++;

		while (!Colors.isWhite(rgb[p.y][y], hsb[p.y][y]) && p.y >= 0)
			p.y--;

		return p;
	}

	private Point2 verticalSwipe(Color[][] rgb, float[][][] hsb, int x) {
		Point2 p = new Point2(0, Vision.HEIGHT - 1);

		while (!Colors.isWhite(rgb[x][p.x], hsb[x][p.x]) && p.x < Vision.HEIGHT)
			p.x++;

		while (!Colors.isWhite(rgb[x][p.y], hsb[x][p.y]) && p.y >= 0)
			p.y--;
		return p;
	}

	/**
	 * Tries to grab pitch data from the current state listener's RGB/HSB values
	 */
	public boolean Initialize(Color[][] rgb, float[][][] hsb) {

		Iterable<Point2> pitchPoints = Vision.stateListener.getPitchPoints();
		int[] xs = new int[Vision.WIDTH];
		int[] ys = new int[Vision.HEIGHT];

		for (Point2 p : pitchPoints) {
			Color cRgb = rgb[p.x][p.y];
			float[] cHsb = hsb[p.x][p.y];
			if (Colors.isWhite(cRgb, cHsb)) {
				xs[p.x]++;
				ys[p.y]++;
			}
		}

		ArrayList<Point2>[] xpts = Alg.getIntervals(xs, 7,
				PITCH_SIDE_X_NPOINTS, PITCH_ZONE_X_NPOINTS);

		ArrayList<Point2>[] ypts = Alg
				.getIntervals(ys, 5, PITCH_SIDE_Y_NPOINTS);

		if (xpts[0].size() < 2 || xpts[1].size() < 3 || ypts[0].isEmpty())
			return false;

		goalLineX[0] = xpts[0].get(0).x;
		goalLineX[1] = xpts[0].get(xpts[0].size() - 1).y;

		for (int i = 0; i < 3; i++) {
			Point2 zone = xpts[1].get(i);
			zoneX[i] = (zone.x + zone.y) / 2;

		}

		pitchY[0] = ypts[0].get(0).x;
		pitchY[1] = ypts[0].get(ypts[0].size() - 1).y;

		Point2 cx = horizontalSwipe(rgb, hsb, pitchY[0] + SOME_SHANO_VALUE);

		Point2 cy = verticalSwipe(rgb, hsb, goalLineX[0] + SOME_SHANO_VALUE);

		pitchCornerX[0] = cx.x;
		pitchCornerX[1] = cx.y;

		goalLineY[0] = cy.x;
		goalLineY[1] = cy.y;

		initialized = true;
		return true;
	}

	public int getYEnd() {
		return Y_END;
	}

	public int getYBegin() {
		return Y_BEGIN;
	}

	public int getXEnd() {
		return X_END;
	}

	public int getXBegin() {
		return X_BEGIN;
	}

	public ArrayList<Point2> getArrayListOfPoints() {
		ArrayList<Point2> points = new ArrayList<Point2>();
		points.add(new Point2()); // 0
		points.add(new Point2(pitchCornerX[0], pitchY[0])); // 1
		points.add(new Point2(zoneX[0], pitchY[0]));// 2
		points.add(new Point2(zoneX[1], pitchY[0])); // 3
		points.add(new Point2(zoneX[2], pitchY[0])); // 4
		points.add(new Point2(pitchCornerX[1], pitchY[0])); // 5
		points.add(new Point2(goalLineX[1], goalLineY[0])); // 6
		points.add(new Point2(goalLineX[1], goalLineY[1]));// 7
		points.add(new Point2(pitchCornerX[1], pitchY[1])); // 8
		points.add(new Point2(zoneX[2], pitchY[1])); // 9
		points.add(new Point2(zoneX[1], pitchY[1])); // 10
		points.add(new Point2(zoneX[0], pitchY[1]));// 11
		points.add(new Point2(pitchCornerX[0], pitchY[1])); // 12
		points.add(new Point2(goalLineX[0], goalLineY[1]));// 13
		points.add(new Point2(goalLineX[0], goalLineY[0])); // 14
		return points;
	}

	public void printPoints() {

		ArrayList<Point2> points = getArrayListOfPoints();
		int index = 1;
		for (Point2 point : points) {
			System.out.println(index + " = " + point.toString());
			index++;
		}
	}

	/**
	 * Gets a point in the centre of the right goal.
	 * 
	 * @return
	 */
	public Point2 getLeftGoalCentre() {
		return new Point2(goalLineX[0], (goalLineY[0] + goalLineY[1]) / 2);
	}

	/**
	 * Gets a point in the centre of the left goal.
	 * 
	 * @return
	 */
	public Point2 getRightGoalCentre() {
		return new Point2(goalLineX[1], (goalLineY[0] + goalLineY[1]) / 2);
	}

	/**
	 * Returns a point in the centre of the 4 pitch corners.
	 * 
	 * @return
	 */
	public Point2 getTableCentre() {
		return new Point2((pitchCornerX[0] + pitchCornerX[1]) / 2,
				(pitchY[0] + pitchY[1]) / 2);
	}

	/**
	 * Method which returns the size of all the borders around the pitch.
	 * Auxiliary method for printPanelDistances.
	 * <p />
	 * On March 1st 2014 I've printed the distances (in pixels) on pitch 0:
	 * <ul>
	 * <li>1->2: 71</li>
	 * <li>2->3: 149</li>
	 * <li>3->4: 150</li>
	 * <li>4->5: 71</li>
	 * <li>5->6: 71.6</li>
	 * <li>6->7: 156</li>
	 * <li>7->8: 69.8</li>
	 * <li>8->9: 71</li>
	 * <li>9->10: 150</li>
	 * <li>10->11: 149</li>
	 * <li>11->12: 71</li>
	 * <li>12->13: 69.4</li>
	 * <li>13->14: 156</li>
	 * <li>14->1: 71.1</li>
	 * </ul>
	 * 
	 * @return
	 */
	private ArrayList<Double> getBorderSizes() {
		ArrayList<Point2> points = getArrayListOfPoints();

		// Calculate border sizes
		ArrayList<Double> dists = new ArrayList<Double>();
		dists.add(points.get(1).distance(points.get(2)));
		dists.add(points.get(2).distance(points.get(3)));
		dists.add(points.get(3).distance(points.get(4)));
		dists.add(points.get(4).distance(points.get(5)));
		dists.add(points.get(5).distance(points.get(6)));
		dists.add(points.get(6).distance(points.get(7)));
		dists.add(points.get(7).distance(points.get(8)));
		dists.add(points.get(8).distance(points.get(9)));
		dists.add(points.get(9).distance(points.get(10)));
		dists.add(points.get(10).distance(points.get(11)));
		dists.add(points.get(11).distance(points.get(12)));
		dists.add(points.get(12).distance(points.get(13)));
		dists.add(points.get(13).distance(points.get(14)));
		dists.add(points.get(14).distance(points.get(1)));
		return dists;
	}

	/**
	 * Method for printing the sizes of the border panels (eg distance from left
	 * goalmouth top to top left corner) - mostly useful for
	 * debugging/calibrating.
	 */
	public void printPanelDistances() {
		ArrayList<Double> dists = getBorderSizes();
		System.out.println("{");
		for (Double d : dists) {
			System.out.println(d);
		}
		System.out.println("}");
	}

	/**
	 * Returns which boundary vertex is closest to a given point.
	 * 
	 * @param collide
	 * @return
	 */
	public Point2 getVertexNearest(Point2 collide) {

		// Initialise required components
		ArrayList<Point2> pts = getArrayListOfPoints();
		double bestDist = Double.POSITIVE_INFINITY;
		Point2 bestPt = Point2.EMPTY;

		// Iterate through all points finding the smallest distance
		for (Point2 pt : pts) {
			double dist = pt.distance(collide);
			if (dist < bestDist) {
				bestDist = dist;
				bestPt = pt;
			}
		}

		// Return the best value
		return bestPt;
	}

	/**
	 * Returns the two panel vertices that are immediately before and after a
	 * given one.
	 * 
	 * @param nearest
	 * @return
	 */
	public Point2[] getBoundariesNeighbouring(Point2 nearest) {
		
		// Initialise required components
		ArrayList<Point2> pts = getArrayListOfPoints();
		int nearestInd = getListIndFromPt(nearest);
		Point2[] rets = new Point2[2];

		// Check corner cases
		if (nearestInd == 0) {
			return null;
		} else if (nearestInd == 1) {
			rets[0] = pts.get(14);
			rets[1] = pts.get(2);
			return rets;
		} else if (nearestInd == 14) {
			rets[0] = pts.get(13);
			rets[1] = pts.get(1);
			return rets;
		} else {

			// No corner case, send the immediate decrement and increment
			rets[0] = pts.get(nearestInd - 1);
			rets[1] = pts.get(nearestInd + 1);
			return rets;
		}
	}

	/**
	 * Returns the index of a point which corresponds to the list in
	 * getArrayListOfPoints() (linear search)
	 * 
	 * @param nearest
	 * @return
	 */
	private int getListIndFromPt(Point2 nearest) {

		// Initialise required components
		ArrayList<Point2> pts = getArrayListOfPoints();

		// Get the index (linear search)
		for (int i = 1; i <= 14; i++) {
			if (nearest.equals(pts.get(i))) {
				return i;
			}
		}

		// Not found? Return 0
		return 0;
	}
}
