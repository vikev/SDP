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
		if(!robotInBounds(p.y, pitchY[0], pitchY[1]))
			return false;
		
		if(Vision.state.getDirection() == 1)	//shoot right => our is left!
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
		if(!robotInBounds(p.y, pitchY[0], pitchY[1]))
			return false;
		
		if(Vision.state.getDirection() == 1)	//shoot right
			return robotInBounds(p.x, zoneX[1], zoneX[2]);
		else
			return robotInBounds(p.x, zoneX[0], zoneX[1]);
	}

	public Pitch() {

	}

	private Point2 horizontalSwipe(Color[][] rgb, float[][][] hsb, int y) {
		Point2 p = new Point2(0, Vision.WIDTH - 1);
		
		while(!Colors.isWhite(rgb[p.x][y], hsb[p.x][y]) && p.x < Vision.WIDTH)
			p.x++;
		
		while(!Colors.isWhite(rgb[p.y][y], hsb[p.y][y]) && p.y >= 0)
			p.y--;
		
		return p;
	}
	
	private Point2 verticalSwipe(Color[][] rgb, float[][][] hsb, int x) {
		Point2 p = new Point2(0, Vision.HEIGHT - 1);
		
		while(!Colors.isWhite(rgb[x][p.x], hsb[x][p.x]) && p.x < Vision.HEIGHT)
			p.x++;
		
		while(!Colors.isWhite(rgb[x][p.y], hsb[x][p.y]) && p.y >= 0)
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
		
		
		for(Point2 p : pitchPoints) {
			Color cRgb = rgb[p.x][p.y];
			float[] cHsb = hsb[p.x][p.y];
			if(Colors.isWhite(cRgb, cHsb)) {
				xs[p.x]++;
				ys[p.y]++;
			}
		}
		
		ArrayList<Point2>[] xpts = Alg.getIntervals(xs, 7,  PITCH_SIDE_X_NPOINTS, PITCH_ZONE_X_NPOINTS);
		
		ArrayList<Point2>[] ypts = Alg.getIntervals(ys, 5, PITCH_SIDE_Y_NPOINTS);
		
		if(xpts[0].size() < 2 || xpts[1].size() < 3 || ypts[0].isEmpty())
			return false;
		
		goalLineX[0] = xpts[0].get(0).x;
		goalLineX[1] = xpts[0].get(xpts[0].size() - 1).y;

		for(int i = 0; i < 3; i++) {
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
}
