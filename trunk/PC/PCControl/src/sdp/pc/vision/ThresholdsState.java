package sdp.pc.vision;

/**
 * Stores the states of the various thresholds.
 * 
 * @author s0840449
 * 
 */
public class ThresholdsState {

	/* Ball. */
	private int ball_r_low;
	private int ball_r_high;
	private int ball_g_low;
	private int ball_g_high;
	private int ball_b_low;
	private int ball_b_high;
	private double ball_h_low;
	private double ball_h_high;
	private double ball_s_low;
	private double ball_s_high;
	private double ball_v_low;
	private double ball_v_high;

	/* Blue Robot. */
	private int blue_r_low;
	private int blue_r_high;
	private int blue_g_low;
	private int blue_g_high;
	private int blue_b_low;
	private int blue_b_high;
	private double blue_h_low;
	private double blue_h_high;
	private double blue_s_low;
	private double blue_s_high;
	private double blue_v_low;
	private double blue_v_high;

	/* Yellow Robot. */
	private int yellow_r_low;
	private int yellow_r_high;
	private int yellow_g_low;
	private int yellow_g_high;
	private int yellow_b_low;
	private int yellow_b_high;
	private double yellow_h_low;
	private double yellow_h_high;
	private double yellow_s_low;
	private double yellow_s_high;
	private double yellow_v_low;
	private double yellow_v_high;

	/* Grey Circle. */
	private int grey_r_low;
	private int grey_r_high;
	private int grey_g_low;
	private int grey_g_high;
	private int grey_b_low;
	private int grey_b_high;
	private double grey_h_low;
	private double grey_h_high;
	private double grey_s_low;
	private double grey_s_high;
	private double grey_v_low;
	private double grey_v_high;

	/* Green plates */
	private int green_r_low;
	private int green_r_high;
	private int green_g_low;
	private int green_g_high;
	private int green_b_low;
	private int green_b_high;
	private double green_h_low;
	private double green_h_high;
	private double green_s_low;
	private double green_s_high;
	private double green_v_low;
	private double green_v_high;

	/* Debug flags. */
	private boolean ball_debug;
	private boolean blue_debug;
	private boolean yellow_debug;
	private boolean grey_debug;
	private boolean green_debug;

	/**
	 * Default constructor.
	 */
	public ThresholdsState() {
	}

	public int getBall_r_low() {
		return ball_r_low;
	}

	public void setBall_r_low(int ballRLow) {
		ball_r_low = ballRLow;
	}

	public int getBall_r_high() {
		return ball_r_high;
	}

	public void setBall_r_high(int ballRHigh) {
		ball_r_high = ballRHigh;
	}

	public int getBall_g_low() {
		return ball_g_low;
	}

	public void setBall_g_low(int ballGLow) {
		ball_g_low = ballGLow;
	}

	public int getBall_g_high() {
		return ball_g_high;
	}

	public void setBall_g_high(int ballGHigh) {
		ball_g_high = ballGHigh;
	}

	public int getBall_b_low() {
		return ball_b_low;
	}

	public void setBall_b_low(int ballBLow) {
		ball_b_low = ballBLow;
	}

	public int getBall_b_high() {
		return ball_b_high;
	}

	public void setBall_b_high(int ballBHigh) {
		ball_b_high = ballBHigh;
	}

	public double getBall_h_low() {
		return ball_h_low;
	}

	public void setBall_h_low(double ballHLow) {
		ball_h_low = ballHLow;
	}

	public double getBall_h_high() {
		return ball_h_high;
	}

	public void setBall_h_high(double ballHHigh) {
		ball_h_high = ballHHigh;
	}

	public double getBall_s_low() {
		return ball_s_low;
	}

	public void setBall_s_low(double ballSLow) {
		ball_s_low = ballSLow;
	}

	public double getBall_s_high() {
		return ball_s_high;
	}

	public void setBall_s_high(double ballSHigh) {
		ball_s_high = ballSHigh;
	}

	public double getBall_v_low() {
		return ball_v_low;
	}

	public void setBall_v_low(double ballVLow) {
		ball_v_low = ballVLow;
	}

	public double getBall_v_high() {
		return ball_v_high;
	}

	public void setBall_v_high(double ballVHigh) {
		ball_v_high = ballVHigh;
	}

	public int getBlue_r_low() {
		return blue_r_low;
	}

	public void setBlue_r_low(int blueRLow) {
		blue_r_low = blueRLow;
	}

	public int getBlue_r_high() {
		return blue_r_high;
	}

	public void setBlue_r_high(int blueRHigh) {
		blue_r_high = blueRHigh;
	}

	public int getBlue_g_low() {
		return blue_g_low;
	}

	public void setBlue_g_low(int blueGLow) {
		blue_g_low = blueGLow;
	}

	public int getBlue_g_high() {
		return blue_g_high;
	}

	public void setBlue_g_high(int blueGHigh) {
		blue_g_high = blueGHigh;
	}

	public int getBlue_b_low() {
		return blue_b_low;
	}

	public void setBlue_b_low(int blueBLow) {
		blue_b_low = blueBLow;
	}

	public int getBlue_b_high() {
		return blue_b_high;
	}

	public void setBlue_b_high(int blueBHigh) {
		blue_b_high = blueBHigh;
	}

	public double getBlue_h_low() {
		return blue_h_low;
	}

	public void setBlue_h_low(double blueHLow) {
		blue_h_low = blueHLow;
	}

	public double getBlue_h_high() {
		return blue_h_high;
	}

	public void setBlue_h_high(double blueHHigh) {
		blue_h_high = blueHHigh;
	}

	public double getBlue_s_low() {
		return blue_s_low;
	}

	public void setBlue_s_low(double blueSLow) {
		blue_s_low = blueSLow;
	}

	public double getBlue_s_high() {
		return blue_s_high;
	}

	public void setBlue_s_high(double blueSHigh) {
		blue_s_high = blueSHigh;
	}

	public double getBlue_v_low() {
		return blue_v_low;
	}

	public void setBlue_v_low(double blueVLow) {
		blue_v_low = blueVLow;
	}

	public double getBlue_v_high() {
		return blue_v_high;
	}

	public int getYellow_r_low() {
		return yellow_r_low;
	}

	public void setYellow_r_low(int yellowRLow) {
		yellow_r_low = yellowRLow;
	}

	public int getYellow_r_high() {
		return yellow_r_high;
	}

	public void setYellow_r_high(int yellowRHigh) {
		yellow_r_high = yellowRHigh;
	}

	public int getYellow_g_low() {
		return yellow_g_low;
	}

	public void setYellow_g_low(int yellowGLow) {
		yellow_g_low = yellowGLow;
	}

	public int getYellow_g_high() {
		return yellow_g_high;
	}

	public void setYellow_g_high(int yellowGHigh) {
		yellow_g_high = yellowGHigh;
	}

	public int getYellow_b_low() {
		return yellow_b_low;
	}

	public void setYellow_b_low(int yellowBLow) {
		yellow_b_low = yellowBLow;
	}

	public int getYellow_b_high() {
		return yellow_b_high;
	}

	public void setYellow_b_high(int yellowBHigh) {
		yellow_b_high = yellowBHigh;
	}

	public double getYellow_h_low() {
		return yellow_h_low;
	}

	public void setYellow_h_low(double yellowHLow) {
		yellow_h_low = yellowHLow;
	}

	public double getYellow_h_high() {
		return yellow_h_high;
	}

	public void setYellow_h_high(double yellowHHigh) {
		yellow_h_high = yellowHHigh;
	}

	public double getYellow_s_low() {
		return yellow_s_low;
	}

	public void setYellow_s_low(double yellowSLow) {
		yellow_s_low = yellowSLow;
	}

	public double getYellow_s_high() {
		return yellow_s_high;
	}

	public void setYellow_s_high(double yellowSHigh) {
		yellow_s_high = yellowSHigh;
	}

	public double getYellow_v_low() {
		return yellow_v_low;
	}

	public void setYellow_v_low(double yellowVLow) {
		yellow_v_low = yellowVLow;
	}

	public double getYellow_v_high() {
		return yellow_v_high;
	}

	public void setYellow_v_high(double yellowVHigh) {
		yellow_v_high = yellowVHigh;
	}

	public void setBlue_v_high(double blueVHigh) {
		blue_v_high = blueVHigh;
	}

	public boolean isBall_debug() {
		return ball_debug;
	}

	public void setBall_debug(boolean ballDebug) {
		ball_debug = ballDebug;
	}

	public boolean isBlue_debug() {
		return blue_debug;
	}

	public void setBlue_debug(boolean blueDebug) {
		blue_debug = blueDebug;
	}

	public boolean isYellow_debug() {
		return yellow_debug;
	}

	public void setYellow_debug(boolean yellowDebug) {
		yellow_debug = yellowDebug;
	}

	public int getGrey_r_low() {
		return grey_r_low;
	}

	public void setGrey_r_low(int greyRLow) {
		grey_r_low = greyRLow;
	}

	public int getGrey_r_high() {
		return grey_r_high;
	}

	public void setGrey_r_high(int greyRHigh) {
		grey_r_high = greyRHigh;
	}

	public int getGrey_g_low() {
		return grey_g_low;
	}

	public void setGrey_g_low(int greyGLow) {
		grey_g_low = greyGLow;
	}

	public int getGrey_g_high() {
		return grey_g_high;
	}

	public void setGrey_g_high(int greyGHigh) {
		grey_g_high = greyGHigh;
	}

	public int getGrey_b_low() {
		return grey_b_low;
	}

	public void setGrey_b_low(int greyBLow) {
		grey_b_low = greyBLow;
	}

	public int getGrey_b_high() {
		return grey_b_high;
	}

	public void setGrey_b_high(int greyBHigh) {
		grey_b_high = greyBHigh;
	}

	public double getGrey_h_low() {
		return grey_h_low;
	}

	public void setGrey_h_low(double greyHLow) {
		grey_h_low = greyHLow;
	}

	public double getGrey_h_high() {
		return grey_h_high;
	}

	public void setGrey_h_high(double greyHHigh) {
		grey_h_high = greyHHigh;
	}

	public double getGrey_s_low() {
		return grey_s_low;
	}

	public void setGrey_s_low(double greySLow) {
		grey_s_low = greySLow;
	}

	public double getGrey_s_high() {
		return grey_s_high;
	}

	public void setGrey_s_high(double greySHigh) {
		grey_s_high = greySHigh;
	}

	public double getGrey_v_low() {
		return grey_v_low;
	}

	public void setGrey_v_low(double greyVLow) {
		grey_v_low = greyVLow;
	}

	public double getGrey_v_high() {
		return grey_v_high;
	}

	public void setGrey_v_high(double greyVHigh) {
		grey_v_high = greyVHigh;
	}

	public boolean isGrey_debug() {
		return grey_debug;
	}

	public void setGrey_debug(boolean greyDebug) {
		grey_debug = greyDebug;
	}

	/**
	 * @return the green_r_low
	 */
	public int getGreen_r_low() {
		return green_r_low;
	}

	public void setGreen_r_low(int greenRLow) {
		green_r_low = greenRLow;
	}

	public int getGreen_r_high() {
		return green_r_high;
	}

	public void setGreen_r_high(int greenRHigh) {
		green_r_high = greenRHigh;
	}

	public int getGreen_g_low() {
		return green_g_low;
	}

	public void setGreen_g_low(int greenGLow) {
		green_g_low = greenGLow;
	}

	public int getGreen_g_high() {
		return green_g_high;
	}

	public void setGreen_g_high(int greenGHigh) {
		green_g_high = greenGHigh;
	}

	public int getGreen_b_low() {
		return green_b_low;
	}

	public void setGreen_b_low(int greenBLow) {
		green_b_low = greenBLow;
	}

	public int getGreen_b_high() {
		return green_b_high;
	}

	public void setGreen_b_high(int greenBHigh) {
		green_b_high = greenBHigh;
	}

	public double getGreen_h_low() {
		return green_h_low;
	}

	public void setGreen_h_low(double greenHLow) {
		green_h_low = greenHLow;
	}

	public double getGreen_h_high() {
		return green_h_high;
	}

	public void setGreen_h_high(double greenHHigh) {
		green_h_high = greenHHigh;
	}

	public double getGreen_s_low() {
		return green_s_low;
	}

	public void setGreen_s_low(double greenSLow) {
		green_s_low = greenSLow;
	}

	public double getGreen_s_high() {
		return green_s_high;
	}

	public void setGreen_s_high(double greenSHigh) {
		green_s_high = greenSHigh;
	}

	public double getGreen_v_low() {
		return green_v_low;
	}

	public void setGreen_v_low(double greenVLow) {
		green_v_low = greenVLow;
	}

	public double getGreen_v_high() {
		return green_v_high;
	}

	public void setGreen_v_high(double greenVHigh) {
		green_v_high = greenVHigh;
	}

	public boolean isGreen_debug() {
		return green_debug;
	}

	public void setGreen_debug(boolean greenDebug) {
		green_debug = greenDebug;
	}

}
