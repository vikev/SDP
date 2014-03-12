package sdp.pc.common;

import java.awt.Color;

/**
 * Class for organising constant data shared among PC classes. Constants is
 * static and shouldn't be instantiated.
 * 
 */
public class Constants {
	/**
	 * Reference one of our two robots
	 */
	public static final int ATTACKER = 1;

	/**
	 * Reference one of our two robots
	 */
	public static final int DEFENDER = 2;

	/**
	 * Port for bluetooth send/receive
	 */
	public static final int ATTACKER_PORT = 5597;

	/**
	 * Port for bluetooth send/receive
	 */
	public static final int DEFENDER_PORT = 1459;

	/**
	 * Port for bluetooth host (can be changed to an IP address but incurs
	 * additional delay)
	 */
	public static final String HOST = "129.215.58.63";

	/**
	 * Approximate measurements of robots in pixels (could change to
	 * centimetres); these values haven't yet been measured. At some point we
	 * can consider writing a centimetre->pixel conversion ratio, which may be
	 * unique to each camera
	 */
	public static final int ATTACKER_LENGTH = 40;

	/**
	 * Approximate measurements of robots in pixels (could change to
	 * centimetres); these values haven't yet been measured. At some point we
	 * can consider writing a centimetre->pixel conversion ratio, which may be
	 * unique to each camera
	 */
	public static final int ATTACKER_WIDTH = 40;

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

	/**
	 * Directions and flags which specify our team's target data in a match
	 */
	public static final int DIRECTION_LEFT = 0;

	/**
	 * Directions and flags which specify our team's target data in a match
	 */
	public static final int DIRECTION_RIGHT = 1;

	/**
	 * Identifiers for team colours
	 */
	public static final int TEAM_YELLOW = 0;

	/**
	 * Identifiers for team colours
	 */
	public static final int TEAM_BLUE = 1;

	/**
	 * Identifiers for left and right robots
	 */
	public static final int ROBOT_LEFT = 0;

	/**
	 * Identifiers for left and right robots
	 */
	public static final int ROBOT_RIGHT = 1;

	/**
	 * Identifier for left yellow robot (0)
	 */
	public static final int ROBOT_YELLOW_LEFT = ROBOT_ID(ROBOT_LEFT,
			TEAM_YELLOW);

	/**
	 * Identifier for left blue robot (1)
	 */
	public static final int ROBOT_BLUE_LEFT = ROBOT_ID(ROBOT_LEFT, TEAM_BLUE);

	/**
	 * Identifier for right yellow robot (2)
	 */
	public static final int ROBOT_YELLOW_RIGHT = ROBOT_ID(ROBOT_RIGHT,
			TEAM_YELLOW);

	/**
	 * Identifier for right blue robot (3)
	 */
	public static final int ROBOT_BLUE_RIGHT = ROBOT_ID(ROBOT_RIGHT, TEAM_BLUE);

	/**
	 * Minimum and maximum X/Y co-ordinates of the table
	 */
	public static final int TABLE_MIN_X = 1;

	/**
	 * Minimum and maximum X/Y co-ordinates of the table
	 */
	public static final int TABLE_MIN_Y = 1;

	/**
	 * Minimum and maximum X/Y co-ordinates of the table
	 */
	public static final int TABLE_MAX_X = 639;

	/**
	 * Minimum and maximum X/Y co-ordinates of the table
	 */
	public static final int TABLE_MAX_Y = 479;

	/**
	 * Center X co-ordinate of the table
	 */
	public static final int TABLE_CENTRE_X = 320;

	/**
	 * The radius of the circle to draw on a robot in the world state painter
	 */
	public static final int ROBOT_CIRCLE_RADIUS = 15;

	/**
	 * The radius of the circle to draw on a black dot found on a robot in the
	 * world state painter
	 */
	public static final int ROBOT_HEAD_RADIUS = 3;

	/**
	 * A Black colour with 50% transparency, useful for drawing gray markers
	 * without obscuring the details of what is below it.
	 */
	public static final Color GRAY_BLEND = new Color(1.0f, 1.0f, 1.0f, 0.5f);

	/**
	 * A Blue colour with 50% transparency, useful for drawing blue circles
	 * without obscuring the details of what is below it
	 */
	public static final Color BLUE_BLEND = new Color(0.0f, 0.0f, 1.0f, 0.5f);

	/**
	 * A Red colour with 50% opacity, useful for drawing red lines without
	 * obscuring the details of what is below it
	 */
	public static final Color RED_BLEND = new Color(1.0f, 0.0f, 0.0f, 0.5f);

	/**
	 * A Red colour with 25% opacity, useful for drawing red lines without
	 * obscuring the details of what is below it
	 */
	public static final Color RED_BLEND_MORE = new Color(1.0f, 0.0f, 0.0f,
			0.25f);
	/**
	 * A Yellow colour with 50% transparency, useful for drawing yellow circles
	 * without obscuring the details of what is below it
	 */
	public static final Color YELLOW_BLEND = new Color(1.0f, 1.0f, 0.0f, 0.5f);

	/**
	 * Method for getting a robot id from a robot side and robot team
	 * 
	 * @param robot
	 *            ROBOT_LEFT or ROBOT_RIGHT
	 * @param team
	 *            TEAM_BLUE or TEAM_YELLOW
	 * @return an integer that uniquely identifies one of the 4 static robots
	 */
	public static final int ROBOT_ID(int robot, int team) {
		return robot * 2 + team;
	}
}
