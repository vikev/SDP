package sdp.pc.vision;

import java.awt.Color;

import sdp.pc.common.Constants;
import sdp.pc.vision.settings.SettingsManager;

/**
 * TODO: What is this class for? Are we using it?
 *
 */
public class Colors {

	/** 
	 * The SettingsManager to get interval values from
	 */
	private static SettingsManager settingsManager;

	/**
	 * Checks whether all components (R,G,B,H,S,V) are greater than the values
	 * in the reference array
	 * @param rgb The RGB colour object
	 * @param hsb The HSB colour object
	 * @param ref 
	 * 		An array of length 6 
	 * 		with the first 3 values (RGB) in the range [0;255]
	 * 		and the last 3 values (HSB) in the range [0;100]
	 * @return Whether all components of the colour are higher than the reference values
	 */
	private static boolean moreThan(Color rgb, float[] hsb, int[] ref) {
		return rgb.getRed() 	>= ref[0]
			&& rgb.getGreen() 	>= ref[1]
			&& rgb.getBlue() 	>= ref[2]
			&& hsb[0] * 100 	>= ref[3]
			&& hsb[1] * 100 	>= ref[4]
			&& hsb[2] * 100	>= ref[5];
	}
	
	/**
	 * Checks whether all components (R,G,B,H,S,V) are less than the values
	 * in the reference array
	 * @param rgb The RGB colour object
	 * @param hsb The HSB colour object
	 * @param ref 
	 * 		An array of length 6 
	 * 		with the first 3 values (RGB) in the range [0;255]
	 * 		and the last 3 values (HSB) in the range [0;100]
	 * @return Whether all components of the colour are higher than the reference values
	 */
	private static boolean lessThan(Color rgb, float[] hsb, int[] ref) {
		return rgb.getRed()	 	<= ref[0]
			&& rgb.getGreen() 	<= ref[1]
			&& rgb.getBlue() 	<= ref[2]
			&& hsb[0] * 100 	<= ref[3]
			&& hsb[1] * 100 	<= ref[4]
			&& hsb[2] * 100 	<= ref[5];
	}
	
	private static boolean around(Color rgb, int[] ref) {
		int 
			dr = Math.abs(rgb.getRed() - ref[0]),
			dg = Math.abs(rgb.getGreen() - ref[1]),
			db = Math.abs(rgb.getBlue() - ref[2]);
		return dr * dr + dg * dg + db * db <= ref[3] * ref[3];
	}
	
	/**
	 * Returns whether the given colour, represented by an RGB and an HSB object matches 
	 * the predicate for a given colour code
	 * @param rgb The RGB colour object
	 * @param hsb The HSB colour object
	 * @param colorCode The colour code to match against
	 * @return Whether the colour matches the given colour code
	 */
	public static boolean isColorCode(Color rgb, float[] hsb, int colorCode) {
		if(rgb == null)
			return false;
		if(SettingsManager.defaultSettings.isUseAltColors())
			return around(rgb, getSettingsManager().getMinValues(colorCode));
		else
			return moreThan(rgb, hsb, getSettingsManager().getMinValues(colorCode))
				&& lessThan(rgb, hsb, getSettingsManager().getMaxValues(colorCode));
	}
	
	/**
	 * Checks if a pixel is black, i.e. can be used for orientation detection
	 * 
	 * @param rgb
	 * @param hsb
	 * @return
	 */
	public static boolean isBlack(Color c, float[] hsb) {
		return isColorCode(c, hsb, SettingsManager.COLOR_CODE_GRAY);
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
		return isColorCode(c, hsb, SettingsManager.COLOR_CODE_PLATE);
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
		if(c == null)
			return false;
		
		int r = c.getRed(),
			g = c.getGreen(),
			b = c.getBlue();
			
		int da = Math.abs(r - g),
			db = Math.abs(r - b),
			dc = Math.abs(g - b);
		
		int d = getSettingsManager().getWhiteRgbDelta(),
			min = getSettingsManager().getWhiteRgbThreshold();
		return da < d && db < d && dc < d &&
				r > min && g > min && b > min;
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
		return isColorCode(c, hsb, SettingsManager.COLOR_CODE_BALL);
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
		return isColorCode(c, hsb, SettingsManager.COLOR_CODE_YELLOW);
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
		return isColorCode(c, hsb, SettingsManager.COLOR_CODE_BLUE);
	}

	public static boolean isTeamColor(int team, Color c, float[] hsb) {
		if (team == Constants.TEAM_BLUE)
			return isBlue(c, hsb);
		else
			return isYellow(c, hsb);
	}

	public static SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public static void setSettingsManager(SettingsManager settingsManager) {
		Colors.settingsManager = settingsManager;
	}

}
