package sdp.pc.common;

import sdp.pc.vision.Point2;

/**
 * Class used to contain static final values.
 * 
 * @author unknown
 */
public class Constants {
	// Socket data
	public static final int PORT = 1313;
	public static final int SERVER_PORT = 1414;
	public static final int CLIENT_PORT = 1515;
	public static final String HOST = "localhost";

	// Arbitrary point values taken from image of pitch; these are likely to
	// change
	private static final int LEFT_GOAL_CENTRE_X = 77, LEFT_GOAL_CENTRE_Y = 235;
	private static final int RIGHT_GOAL_CENTRE_X = 589,
			RIGHT_GOAL_CENTRE_Y = 241;

	// Points which can be referenced
	public static final Point2 LEFT_GOAL_CENTRE = new Point2(
			LEFT_GOAL_CENTRE_X, LEFT_GOAL_CENTRE_Y);
	public static final Point2 RIGHT_GOAL_CENTRE = new Point2(
			RIGHT_GOAL_CENTRE_X, RIGHT_GOAL_CENTRE_Y);

	// Directions and targets, should probably use an Enum, but I think this is
	// related to Constants
	public static final int GOAL_LEFT = 0, GOAL_RIGHT = 1;
	public static final int DIRECTION_LEFT = 0, DIRECTION_RIGHT = 1;

	/*public static final int TABLE_MIN_X = 60, TABLE_MIN_Y = 90,
			TABLE_MAX_X = 600, TABLE_MAX_Y = 380;
	public static final int TABLE_CENTRE_X = 330;*/
	public static final int TABLE_MIN_X = 1, TABLE_MIN_Y = 1,
	TABLE_MAX_X = 639, TABLE_MAX_Y = 479;
	public static final int TABLE_CENTRE_X = 330;
	
	public static final int ROBOT_CIRCLE_RADIUS = 15;
}
