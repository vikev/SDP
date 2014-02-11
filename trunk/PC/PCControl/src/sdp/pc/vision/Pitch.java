package sdp.pc.vision;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Contains information about the Pitch dimensions and the zones a robot is supposed to be in. 
 * Currently only checks X values; works well given that Colors.isWhite() returns proper values
 * 
 * TODO: add Y values checks
 * TODO: detect goal positions automatically (would it be reliable enough?)
 * 
 * @author s1141301
 *
 */
public class Pitch {
	
	/**
	 * The radius of a robot
	 * TODO: figure out a value for this
	 */
	private int robotRadius = 0;
	
	/**
	 * The x coordinates of the goal lines
	 */
	private int[] goalLineX = new int[2];
	/**
	 * The x coordinates of the (middle of the) zones
	 */
	private int[] zoneX = new int[3];

	
	/**
	 * Gets whether the specified point is in our defender zone.
	 * Uses Vision.state.getDirection() to determine our team.
	 * @param p the point to check
	 */
	public boolean isPointInDefenderZone(Point2 p) {
		if(Vision.state.getDirection() == 1)	//shoot right => our is left!
			return robotInBounds(p.x, goalLineX[0], zoneX[0]);
		else
			return robotInBounds(p.x, zoneX[2], goalLineX[1]);
	}
	
	/**
	 * Checks whether the specified value is in the range [min + robotRadius; max - robotRadius]
	 */
	private boolean robotInBounds(int val, int min, int max) {
		return val >= min + robotRadius && val <= max - robotRadius;
	}

	/**
	 * Gets whether the specified point is in our attacker zone
	 * Uses Vision.state.getDirection() to determine our team.
	 * @param p the point to check
	 */
	public boolean isPointInAttackerZone(Point2 p) {
		if(Vision.state.getDirection() == 1)	//shoot right
			return robotInBounds(p.x, zoneX[1], zoneX[2]);
		else
			return robotInBounds(p.x, zoneX[0], zoneX[1]);
	}
	
	
	public Pitch() {

	}
	
	/**
	 * Grabs pitch data from the current state listener's RGB/HSB values
	 */
	public void Initialize() {
		Iterable<Point2> pitchPoints = Vision.stateListener.getPitchPoints();
		int[] xs = new int[Vision.WIDTH];
		int[] ys = new int[Vision.HEIGHT];
		
		int minX = Vision.WIDTH, 
			maxX = 0,
			minY = Vision.HEIGHT,
			maxY = 0;
		
		for(Point2 p : pitchPoints) {
			Color cRgb = Vision.stateListener.getRgb(p.x, p.y);
			float[] cHsb = Vision.stateListener.getNormalisedHsb(p.x, p.y);
			if(Colors.isWhite(cRgb, cHsb)) {
				xs[p.x]++;
				ys[p.y]++;
				if(minX > p.x)
					minX = p.x;
				if(maxX < p.x) 
					maxX = p.x;
				if(minY > p.y)
					minY = p.y;
				if(maxY < p.y) 
					maxY = p.y;
			}
		}
		
		ArrayList<Point2>[] pts = Alg.getIntervals(xs, 5,  99, 175);
		
		assert pts[0].size() > 1;
		assert pts[1].size() == 3;
		
		goalLineX[0] = pts[0].get(0).x;
		goalLineX[1] = pts[0].get(pts[0].size() - 1).y;

		for(int i = 0; i < 3; i++) {
			Point2 zone = pts[1].get(i);
			zoneX[i] = (zone.x + zone.y) / 2;
		}
	}
}