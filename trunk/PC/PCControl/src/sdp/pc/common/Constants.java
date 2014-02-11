package sdp.pc.common;

import java.awt.Color;

import sdp.pc.vision.Point2;

/**
 * Class used to contain static final values.
 * 
 * @author unknown
 */
public class Constants {
	// Robot codes
	public static final int ATTACKER = 1;
	public static final int DEFENDER = 2;

	// Socket data
	public static final int ATTACKER_PORT = 5595;
	public static final int DEFENDER_PORT = 1451;
	public static final String HOST = "localhost";

	/**
	 * Approximate measurements of robots in pixels (could change to
	 * centimetres); these values haven't yet been measured. At some point we
	 * can consider writing a centimetre->pixel conversion ratio, which may be
	 * unique to each camera
	 */
	public static final int ATTACKER_LENGTH = 30;

	/**
	 * Approximate measurements of robots in pixels (could change to
	 * centimetres); these values haven't yet been measured. At some point we
	 * can consider writing a centimetre->pixel conversion ratio, which may be
	 * unique to each camera
	 */
	public static final int ATTACKER_WIDTH = 30;
	
	/**
	 * Approximate measurements of robots in pixels (could change to
	 * centimetres); these values haven't yet been measured. At some point we
	 * can consider writing a centimetre->pixel conversion ratio, which may be
	 * unique to each camera
	 */
	public static final int DEFENDER_LENGTH = 20;
	
	/**
	 * Approximate measurements of robots in pixels (could change to
	 * centimetres); these values haven't yet been measured. At some point we
	 * can consider writing a centimetre->pixel conversion ratio, which may be
	 * unique to each camera
	 */
	public static final int DEFENDER_WIDTH = 20;

	// Directions and targets, should probably use an Enum, but I think this is
	// related to Constants
	public static final int DIRECTION_LEFT = 0, DIRECTION_RIGHT = 1;
	public static final int TEAM_YELLOW = 0, TEAM_BLUE = 1,

	ROBOT_LEFT = 0, ROBOT_RIGHT = 1,

	ROBOT_YELLOW_LEFT = ROBOT_ID(ROBOT_LEFT, TEAM_YELLOW), // 0
			ROBOT_BLUE_LEFT = ROBOT_ID(ROBOT_LEFT, TEAM_BLUE), // 1
			ROBOT_YELLOW_RIGHT = ROBOT_ID(ROBOT_RIGHT, TEAM_YELLOW),// 2
			ROBOT_BLUE_RIGHT = ROBOT_ID(ROBOT_RIGHT, TEAM_BLUE); // 3

	public static final int ROBOT_ID(int robot, int team) {
		return robot * 2 + team;
	}

	public static final int TABLE_MIN_X = 1, TABLE_MIN_Y = 1,
			TABLE_MAX_X = 639, TABLE_MAX_Y = 479, TABLE_CENTRE_X = 320;

	public static final int ROBOT_CIRCLE_RADIUS = 15, ROBOT_HEAD_RADIUS = 3,
			HEAD_ARC_FIDELITY = 36;

	public static final Color GRAY_BLEND = new Color(1.0f, 1.0f, 1.0f, 0.5f),
			BLUE_BLEND = new Color(0.0f, 0.0f, 1.0f, 0.5f),
			YELLOW_BLEND = new Color(1.0f, 1.0f, 0.0f, 0.5f);

	public static final double HEAD_ENUM_RADIUS = 12.0;
}
