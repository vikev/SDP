package sdp.pc.vision;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

import sdp.pc.common.Constants;

/**
 * Contains the actual code used to process (normalise) the colours on an image.
 * And the code to read the image and update world state
 * @author s1141301
 *
 */
public class WorldStateUpdater extends WorldStateListener {

	//TODO: add minimum count of points for each robot's position
	public int MINIMUM_ROBOT_POINTS = 10;
	public int PAST_BALL_POSITIONS = 10;
	
	//used to find them correct ball/robot positions
	//[team][robot]
	private int[][] robotPtsCount = new int[2][2];
	private Point2[][] robotPos = new Point2[2][2];
	@SuppressWarnings("unchecked")	//can't create generic arrays
	private ArrayList<Point2>[][] robotPts = new ArrayList[][] {
		new ArrayList[] {
			new ArrayList<Point2>(),
			new ArrayList<Point2>(),
		},
		new ArrayList[] {
			new ArrayList<Point2>(),
			new ArrayList<Point2>()
		}
	};
	
	private int ballPtsCount;
	Point2 ballPos;
	LinkedList<Point2> ballPastPos = new LinkedList<Point2>();
	
	
	
	public WorldStateUpdater(int targetFps, WorldState state) {
		super(targetFps, state);
	}

	
	@Override
	public void updateWorld(Color[][] cRgbs, float[][][] cHsbs) {

		//reset color recognition values
		//bet its faster than allocating new array lists every time *untested*
		ballPtsCount = 0;
		ballPos = new Point2();
		for(int i = 0; i < 2; i++)
			for(int j = 0; j < 2; j++) {
			robotPtsCount[i][j] = 0;
			robotPts[i][j].clear();
			robotPos[i][j] = new Point2();
		}
		
		Color cRgb;
		float[] cHsb;
		int x,y;
		int robotSide;	//whether this point is on the left or right side of the pitch
		
		// Loop through all table values, recognising pixel regions as necessary
		for (Point2 p : pitchPoints) {
			
			x = p.getX();
			y = p.getY();
			
			
			cRgb = cRgbs[x][y];
			cHsb = cHsbs[x][y];

			//check if it's a ball color
			if (Colors.isBall(cRgb, cHsb)) {
				//set vals
				ballPos = ballPos.add(p);
				ballPtsCount++;
			}
			
			//check if it's a team color
			for(int team = 0; team < 2; team++) {
				if(Colors.isTeamColor(team, cRgb, cHsb)) {
					
					//get relevant robot
					if(x < Constants.TABLE_CENTRE_X)
						robotSide = Constants.ROBOT_LEFT;
					else
						robotSide = Constants.ROBOT_RIGHT;
					
					//set vals
					robotPos[team][robotSide] = robotPos[team][robotSide].add(p);
					robotPtsCount[team][robotSide]++;
					robotPts[team][robotSide].add(p);
				}
				
			}
		}

		//update ball position
		if (ballPtsCount > 0) {
			ballPos = ballPos.div(ballPtsCount);
			
			//get average of past positions (if any)
			if(ballPastPos.size() > 0) {
				Point2 avgPastPos = new Point2();
				for (Point2 p : ballPastPos)
					avgPastPos = avgPastPos.add(p);
				avgPastPos = avgPastPos.div(ballPastPos.size());
				
				avgPastPos = avgPastPos.subtract(ballPos);
				state.setBallVelocity(avgPastPos);
			}
			
			//add to past positions and trim (if necessary)
			ballPastPos.addFirst(ballPos);
			if(ballPastPos.size() > PAST_BALL_POSITIONS)
				ballPastPos.removeLast();
			
			state.setBallPosition(ballPos);
		}

		
		//update team positions
		for(int team = 0; team < 2; team++)
			for(int robot = 0; robot < 2; robot++) {
				//check if we saw that robot
				int ptCount = robotPtsCount[team][robot];
				if(ptCount > MINIMUM_ROBOT_POINTS) {
					//if so, find center
					Point2 newPos = robotPos[team][robot].div(ptCount);

					//remove outliers and find it again
					ArrayList<Point2> newPts = Point2.removeOutliers(
							robotPts[team][robot], newPos);
					
					newPos.filterPoints(newPts);
					
					//find facing, as well
					double newFacing = findOrientation(newPos, cRgbs, cHsbs);
					
					//update the world state
					state.setRobotPosition(team, robot, newPos);
					state.setRobotFacing(team, robot, newFacing);
				}
			}
	}
	
	
	
	/**
	 * Finds the orientation a robot is facing given its centroid.
	 * Works by first finding all green pixels and calculating their convex hull.
	 * Then finds all black pixels inside that hull and calculates their mean.
	 * Finally returns the angle between the centroid and the black-point-mean
  	 * @author s1143704, s1141301
	 * @param p the centroid of the robot
	 * @param cRgbs the RGB colors of the image
	 * @param cHsbs the HSB colors of the image
	 * @return the angle the robot is facing
	 */
	private double findOrientation(Point2 p, Color[][] cRgbs, float[][][] cHsbs) {
		int x = p.getX();
		int y = p.getY();
		
		final int RECT_SIZE = 25;
		int minX = x - RECT_SIZE,
			maxX = x + RECT_SIZE,
			minY = y - RECT_SIZE,
			maxY = y + RECT_SIZE;
		
		Color cRgb;
		float[] cHsb;

		//find all green pixels
		ArrayList<Point2> greenPoints = new ArrayList<Point2>();
		for (int ix = minX; ix < maxX; ix++)
			for (int iy = minY; iy < maxY; iy++) {
				if (Alg.pointInPitch(x, y)) {
					cRgb = cRgbs[x][y];
					cHsb = cHsbs[x][y];
					if (Colors.isGreen(cRgb, cHsb)) {
						greenPoints.add(new Point2(x, y));
					}
				}
			}
		
		if(greenPoints.isEmpty())
			return 0;	//no green pts :(
		
		//calculate the hull of the green points
		LinkedList<Point2> hull = Alg.convexHull(greenPoints);
		
		//now search for black points 
		Point2 blackPos = new Point2();
		int blackCount = 0;
		for (int ix = minX; ix < maxX; ix++)
			for (int iy = minY; iy < maxY; iy++) {
				Point2 ip = new Point2(ix,iy);
				if(Colors.isBlack(cRgbs[x][y], cHsbs[x][y]))
					if(Alg.isInHull(hull, ip)) {
						blackCount++;
						blackPos = blackPos.add(ip);
					}
			}

		if(blackCount == 0)
			return 0;	//no black pts :(
		
		blackPos = blackPos.div(blackCount);
		
		return blackPos.angleTo(p);
	}

	
	/**
	 * An implementation of processImage which updates the given arrays' values 
	 * with the corresponding normalised colour values from the given image
	 */
	@Override
	protected void processImage(BufferedImage img, Color[][] cRgbs, float[][][] cHsbs) {
		Color cRgb;
		float[] cHsb;
		for(Point2 p : pitchPoints) {
			
			//get RGB handle
			cRgb = new Color(img.getRGB(p.x, p.y));

			//get HSB handle
			cHsb = cHsbs[p.x][p.y];
			Color.RGBtoHSB(cRgb.getRed(), cRgb.getGreen(), cRgb.getBlue(), cHsb);

			//scale HSB
			float br = cHsb[2];
			
			// minMaxBrightness is of the form {min, max}
			br = (br - minMaxBrightness[0]) / (minMaxBrightness[1] - minMaxBrightness[0]);
			br = Math.min(1, Math.max(0, br));
			
			cHsb[2] = br;
			
			//save RGB
			cRgbs[p.x][p.y] = new Color(Color.HSBtoRGB(cHsb[0], cHsb[1], cHsb[2]));
			//HSB is up to date
			
		}
		
	}	
}
