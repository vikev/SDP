package sdp.pc.vision;

import java.awt.Color;

import sdp.pc.common.Constants;

public class Colors {

	private static ThresholdsState tresholds;

	/**
	 * Checks if a pixel is black, i.e. can be used for orientation detection
	 * 
	 * @param rgb
	 * @param hsb
	 * @return
	 */
	public static boolean isBlack(Color c, float[] hsb) {
		if (c instanceof Color) {
			return hsb[0] <= tresholds.getGrey_h_high()
					&& hsb[0] >= tresholds.getGrey_h_low()
					&& hsb[1] <= tresholds.getGrey_s_high()
					&& hsb[1] >= tresholds.getGrey_s_low()
					&& hsb[2] <= tresholds.getGrey_v_high()
					&& hsb[2] >= tresholds.getGrey_v_low()
					&& c.getRed() <= tresholds.getGrey_r_high()
					&& c.getRed() >= tresholds.getGrey_r_low()
					&& c.getGreen() <= tresholds.getGrey_g_high()
					&& c.getGreen() >= tresholds.getGrey_g_low()
					&& c.getBlue() <= tresholds.getGrey_b_high()
					&& c.getBlue() >= tresholds.getGrey_b_low();
		}
		return false;
	}

	/**
	 * Determines if a pixel is from the green plates that hold the yellow and
	 * blue i's. Used to calculate the polygons that determine the plates in
	 * order to look for the black dot within them.
	 * 
	 * @param rgb
	 *            The RGB colours for the pixel.
	 * @param hsb
	 *            The HSV values for the pixel.
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the green plates), false
	 *         otherwise.
	 */
	public static boolean isGreen(Color c, float[] hsb) {
		return hsb[0] <= tresholds.getGreen_h_high()
				&& hsb[0] >= tresholds.getGreen_h_low()
				&& hsb[1] <= tresholds.getGreen_s_high()
				&& hsb[1] >= tresholds.getGreen_s_low()
				&& hsb[2] <= tresholds.getGreen_v_high()
				&& hsb[2] >= tresholds.getGreen_v_low()
				&& c.getRed() <= tresholds.getGreen_r_high()
				&& c.getRed() >= tresholds.getGreen_r_low()
				&& c.getGreen() <= tresholds.getGreen_g_high()
				&& c.getGreen() >= tresholds.getGreen_g_low()
				&& c.getBlue() <= tresholds.getGreen_b_high()
				&& c.getBlue() >= tresholds.getGreen_b_low();
	}

	/**
	 * Determines if a pixel is from the white tape on the pitch. Used to
	 * calculate the smallest possible polygon that captures the entire pitch.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of white tape), false otherwise.
	 */
	public static boolean isWhite(Color c, float[] hsb) {
		boolean h = Alg.withinBounds(hsb[0], 0.00f, 1f);
		boolean s = Alg.withinBounds(hsb[1], 0.05f, 0.3f);
		boolean b = Alg.withinBounds(hsb[2], 0.65f, 0.7f);
		if (c != null) {
			final int delta = 100;
			final double minRgb = 150;
			int r = c.getRed();
			int g = c.getGreen();
			int bl = c.getBlue();
			boolean rgb = Math.abs(r - g) < delta && Math.abs(r - bl) < delta
					&& Math.abs(bl - g) < delta && g > minRgb;
			//return rgb && h && s && b;
			return h && s;
		} else {
			return false;
		}
	}

	/**
	 * Determines if a pixel is part of the ball, based on input RGB colours and
	 * hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsb
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the ball), false otherwise.
	 */
	public static boolean isBall(Color c, float[] hsb) {
		return hsb[0] <= tresholds.getBall_h_high()
				&& hsb[0] >= tresholds.getBall_h_low()
				&& hsb[1] <= tresholds.getBall_s_high()
				&& hsb[1] >= tresholds.getBall_s_low()
				&& hsb[2] <= tresholds.getBall_v_high()
				&& hsb[2] >= tresholds.getBall_v_low()
				&& c.getRed() <= tresholds.getBall_r_high()
				&& c.getRed() >= tresholds.getBall_r_low()
				&& c.getGreen() <= tresholds.getBall_g_high()
				&& c.getGreen() >= tresholds.getBall_g_low()
				&& c.getBlue() <= tresholds.getBall_b_high()
				&& c.getBlue() >= tresholds.getBall_b_low();
	}

	/**
	 * Determines if a pixel is part of the yellow T, based on input RGB colours
	 * and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsb
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the yellow T), false otherwise.
	 */
	public static boolean isYellow(Color c, float[] hsb) {
		return hsb[0] <= tresholds.getYellow_h_high()
				&& hsb[0] >= tresholds.getYellow_h_low()
				&& hsb[1] <= tresholds.getYellow_s_high()
				&& hsb[1] >= tresholds.getYellow_s_low()
				&& hsb[2] <= tresholds.getYellow_v_high()
				&& hsb[2] >= tresholds.getYellow_v_low()
				&& c.getRed() <= tresholds.getYellow_r_high()
				&& c.getRed() >= tresholds.getYellow_r_low()
				&& c.getGreen() <= tresholds.getYellow_g_high()
				&& c.getGreen() >= tresholds.getYellow_g_low()
				&& c.getBlue() <= tresholds.getYellow_b_high()
				&& c.getBlue() >= tresholds.getYellow_b_low();
	}

	/**
	 * Determines if a pixel is part of the blue T, based on input RGB colours
	 * and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsb
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the blue T), false otherwise.
	 */
	public static boolean isBlue(Color c, float[] hsb) {
		return hsb[0] <= tresholds.getBlue_h_high()
				&& hsb[0] >= tresholds.getBlue_h_low()
				&& hsb[1] <= tresholds.getBlue_s_high()
				&& hsb[1] >= tresholds.getBlue_s_low()
				&& hsb[2] <= tresholds.getBlue_v_high()
				&& hsb[2] >= tresholds.getBlue_v_low()
				&& c.getRed() <= tresholds.getBlue_r_high()
				&& c.getRed() >= tresholds.getBlue_r_low()
				&& c.getGreen() <= tresholds.getBlue_g_high()
				&& c.getGreen() >= tresholds.getBlue_g_low()
				&& c.getBlue() <= tresholds.getBlue_b_high()
				&& c.getBlue() >= tresholds.getBlue_b_low();
	}

	public static void setTreshold(ThresholdsState tresholds) {
		Colors.tresholds = tresholds;
	}

	public static boolean isTeamColor(int team, Color c, float[] hsb) {
		if (team == Constants.TEAM_BLUE)
			return isBlue(c, hsb);
		else
			return isYellow(c, hsb);
	}

}
