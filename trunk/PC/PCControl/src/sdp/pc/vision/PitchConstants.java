package sdp.pc.vision;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * A state object that holds the constants for various values about the pitch,
 * such as thresholding values and dimension variables.
 * 
 * @author s0840449
 */
public class PitchConstants {

	/** The pitch number. 0 is the main pitch, 1 is the side pitch. */
	private int pitchNum;

	/* Settings */
	public int ourTeam; // 0 = Yellow; 1 = Blue
	public int shootingDirection; // 0 = Left; 1 = Right

	/* Ball */
	public int ball_r_low;
	public int ball_r_high;
	public int ball_g_low;
	public int ball_g_high;
	public int ball_b_low;
	public int ball_b_high;
	public int ball_h_low;
	public int ball_h_high;
	public int ball_s_low;
	public int ball_s_high;
	public int ball_v_low;
	public int ball_v_high;

	/* Blue Robot */
	public int blue_r_low;
	public int blue_r_high;
	public int blue_g_low;
	public int blue_g_high;
	public int blue_b_low;
	public int blue_b_high;
	public int blue_h_low;
	public int blue_h_high;
	public int blue_s_low;
	public int blue_s_high;
	public int blue_v_low;
	public int blue_v_high;

	/* Yellow Robot */
	public int yellow_r_low;
	public int yellow_r_high;
	public int yellow_g_low;
	public int yellow_g_high;
	public int yellow_b_low;
	public int yellow_b_high;
	public int yellow_h_low;
	public int yellow_h_high;
	public int yellow_s_low;
	public int yellow_s_high;
	public int yellow_v_low;
	public int yellow_v_high;

	/* Grey Circles */
	public int grey_r_low;
	public int grey_r_high;
	public int grey_g_low;
	public int grey_g_high;
	public int grey_b_low;
	public int grey_b_high;
	public int grey_h_low;
	public int grey_h_high;
	public int grey_s_low;
	public int grey_s_high;
	public int grey_v_low;
	public int grey_v_high;

	/* Green plates */
	public int green_r_low;
	public int green_r_high;
	public int green_g_low;
	public int green_g_high;
	public int green_b_low;
	public int green_b_high;
	public int green_h_low;
	public int green_h_high;
	public int green_s_low;
	public int green_s_high;
	public int green_v_low;
	public int green_v_high;

	/*
	 * Pitch dimensions: When scanning the pitch we look at pixels starting from
	 * 0 + topBuffer and 0 + leftBuffer, and then scan to pixels at 480 -
	 * bottomBuffer and 640 - rightBuffer.
	 */
	public int topBuffer;
	public int bottomBuffer;
	public int leftBuffer;
	public int rightBuffer;

	/*
	 * Location of the goals
	 */
	private Point2 leftGoalTop;
	private Point2 leftGoalBottom;
	private Point2 rightGoalTop;
	private Point2 rightGoalBottom;
	private Point2 leftGoalCentre;
	private Point2 rightGoalCentre;

	/**
	 * Default constructor.
	 * 
	 * @param pitchNum
	 *            The pitch that we are on.
	 */
	public PitchConstants(int pitchNum) {

		/* Just call the setPitchNum method to load in the constants. */
		setPitchNum(pitchNum);

	}

	/**
	 * Sets a new pitch number, loading in constants from the corresponding
	 * file.
	 * 
	 * @param newPitchNum
	 *            The pitch number to use.
	 */
	public void setPitchNum(int newPitchNum) {

		assert (newPitchNum >= 0 && newPitchNum <= 1);

		this.pitchNum = newPitchNum;

		loadConstants("./pitch" + pitchNum);

	}

	/**
	 * Load in the constants from a file. Note that this assumes that the
	 * constants file is well formed.
	 * 
	 * @param fileName
	 *            The file name to load constants from.
	 */
	public void loadConstants(String fileName) {

		Scanner scanner = null;

		try {
			scanner = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			System.err.println("Cannot load constants file " + fileName + ":");
			System.err.println(e.getMessage());
			loadDefaultConstants();
			return;
		}

		assert (scanner != null);

		/* We assume that the file is well formed. */

		/* Settings */
		this.ourTeam = scanner.nextInt();
		this.shootingDirection = scanner.nextInt();

		/* Ball */
		this.ball_r_low = scanner.nextInt();
		this.ball_r_high = scanner.nextInt();
		this.ball_g_low = scanner.nextInt();
		this.ball_g_high = scanner.nextInt();
		this.ball_b_low = scanner.nextInt();
		this.ball_b_high = scanner.nextInt();
		this.ball_h_low = scanner.nextInt();
		this.ball_h_high = scanner.nextInt();
		this.ball_s_low = scanner.nextInt();
		this.ball_s_high = scanner.nextInt();
		this.ball_v_low = scanner.nextInt();
		this.ball_v_high = scanner.nextInt();

		/* Blue Robot */
		this.blue_r_low = scanner.nextInt();
		this.blue_r_high = scanner.nextInt();
		this.blue_g_low = scanner.nextInt();
		this.blue_g_high = scanner.nextInt();
		this.blue_b_low = scanner.nextInt();
		this.blue_b_high = scanner.nextInt();
		this.blue_h_low = scanner.nextInt();
		this.blue_h_high = scanner.nextInt();
		this.blue_s_low = scanner.nextInt();
		this.blue_s_high = scanner.nextInt();
		this.blue_v_low = scanner.nextInt();
		this.blue_v_high = scanner.nextInt();

		/* Yellow Robot */
		this.yellow_r_low = scanner.nextInt();
		this.yellow_r_high = scanner.nextInt();
		this.yellow_g_low = scanner.nextInt();
		this.yellow_g_high = scanner.nextInt();
		this.yellow_b_low = scanner.nextInt();
		this.yellow_b_high = scanner.nextInt();
		this.yellow_h_low = scanner.nextInt();
		this.yellow_h_high = scanner.nextInt();
		this.yellow_s_low = scanner.nextInt();
		this.yellow_s_high = scanner.nextInt();
		this.yellow_v_low = scanner.nextInt();
		this.yellow_v_high = scanner.nextInt();

		/* Grey Circles */
		this.grey_r_low = scanner.nextInt();
		this.grey_r_high = scanner.nextInt();
		this.grey_g_low = scanner.nextInt();
		this.grey_g_high = scanner.nextInt();
		this.grey_b_low = scanner.nextInt();
		this.grey_b_high = scanner.nextInt();
		this.grey_h_low = scanner.nextInt();
		this.grey_h_high = scanner.nextInt();
		this.grey_s_low = scanner.nextInt();
		this.grey_s_high = scanner.nextInt();
		this.grey_v_low = scanner.nextInt();
		this.grey_v_high = scanner.nextInt();

		/* Green Plates */
		this.green_r_low = scanner.nextInt();
		this.green_r_high = scanner.nextInt();
		this.green_g_low = scanner.nextInt();
		this.green_g_high = scanner.nextInt();
		this.green_b_low = scanner.nextInt();
		this.green_b_high = scanner.nextInt();
		this.green_h_low = scanner.nextInt();
		this.green_h_high = scanner.nextInt();
		this.green_s_low = scanner.nextInt();
		this.green_s_high = scanner.nextInt();
		this.green_v_low = scanner.nextInt();
		this.green_v_high = scanner.nextInt();

		// TODO: test!!!
		Point2 pa = new Point2(scanner.nextInt(), scanner.nextInt());
		Point2 pb = new Point2(scanner.nextInt(), scanner.nextInt());
		WorldStateListener.resetBoundary(); // just in case
		WorldStateListener.addBoundary(pa); // order doesn't matter
		WorldStateListener.addBoundary(pb);

		/* Pitch Dimensions */
		// TODO: test
		this.topBuffer = scanner.nextInt();
		this.bottomBuffer = scanner.nextInt();
		this.leftBuffer = scanner.nextInt();
		this.rightBuffer = scanner.nextInt();
		this.leftGoalTop = new Point2(scanner.nextInt(), scanner.nextInt());
		this.leftGoalBottom = new Point2(scanner.nextInt(), scanner.nextInt());
		this.rightGoalTop = new Point2(scanner.nextInt(), scanner.nextInt());
		this.rightGoalBottom = new Point2(scanner.nextInt(), scanner.nextInt());

		this.leftGoalCentre = new Point2(
				(int) (this.leftGoalTop.getX() + this.leftGoalBottom.getX()) / 2,
				(int) (this.leftGoalTop.getY() + this.leftGoalBottom.getY()) / 2);
		this.rightGoalCentre = new Point2(
				(int) (this.rightGoalTop.getX() + this.rightGoalBottom.getX()) / 2,
				(int) (this.rightGoalTop.getY() + this.rightGoalBottom.getY()) / 2);
		if (scanner != null)
			scanner.close();
	}

	/**
	 * Loads its values to ThresholdsStat382e
	 */
	public void uploadConstants(ThresholdsState thresh, WorldState state) {

		// Settings
		state.setOurColor(ourTeam);
		state.setDirection(shootingDirection);

		// Ball
		thresh.setBall_r_low(this.ball_r_low);
		thresh.setBall_r_high(this.ball_r_high);
		thresh.setBall_g_low(this.ball_g_low);
		thresh.setBall_g_high(this.ball_g_high);
		thresh.setBall_b_low(this.ball_b_low);
		thresh.setBall_b_high(this.ball_b_high);
		thresh.setBall_h_low(this.ball_h_low / 100.0);
		thresh.setBall_h_high(this.ball_h_high / 100.0);
		thresh.setBall_s_low(this.ball_s_low / 100.0);
		thresh.setBall_s_high(this.ball_s_high / 100.0);
		thresh.setBall_v_low(this.ball_v_low / 100.0);
		thresh.setBall_v_high(this.ball_v_high / 100.0);

		// Blue Robots
		thresh.setBlue_r_low(this.blue_r_low);
		thresh.setBlue_r_high(this.blue_r_high);
		thresh.setBlue_g_low(this.blue_g_low);
		thresh.setBlue_g_high(this.blue_g_high);
		thresh.setBlue_b_low(this.blue_b_low);
		thresh.setBlue_b_high(this.blue_b_high);
		thresh.setBlue_h_low(this.blue_h_low / 100.0);
		thresh.setBlue_h_high(this.blue_h_high / 100.0);
		thresh.setBlue_s_low(this.blue_s_low / 100.0);
		thresh.setBlue_s_high(this.blue_s_high / 100.0);
		thresh.setBlue_v_low(this.blue_v_low / 100.0);
		thresh.setBlue_v_high(this.blue_v_high / 100.0);

		// Yellow Robots
		thresh.setYellow_r_low(this.yellow_r_low);
		thresh.setYellow_r_high(this.yellow_r_high);
		thresh.setYellow_g_low(this.yellow_g_low);
		thresh.setYellow_g_high(this.yellow_g_high);
		thresh.setYellow_b_low(this.yellow_b_low);
		thresh.setYellow_b_high(this.yellow_b_high);
		thresh.setYellow_h_low(this.yellow_h_low / 100.0);
		thresh.setYellow_h_high(this.yellow_h_high / 100.0);
		thresh.setYellow_s_low(this.yellow_s_low / 100.0);
		thresh.setYellow_s_high(this.yellow_s_high / 100.0);
		thresh.setYellow_v_low(this.yellow_v_low / 100.0);
		thresh.setYellow_v_high(this.yellow_v_high / 100.0);

		// Grey Circles
		thresh.setGrey_r_low(this.grey_r_low);
		thresh.setGrey_r_high(this.grey_r_high);
		thresh.setGrey_g_low(this.grey_g_low);
		thresh.setGrey_g_high(this.grey_g_high);
		thresh.setGrey_b_low(this.grey_b_low);
		thresh.setGrey_b_high(this.grey_b_high);
		thresh.setGrey_h_low(this.grey_h_low / 100.0);
		thresh.setGrey_h_high(this.grey_h_high / 100.0);
		thresh.setGrey_s_low(this.grey_s_low / 100.0);
		thresh.setGrey_s_high(this.grey_s_high / 100.0);
		thresh.setGrey_v_low(this.grey_v_low / 100.0);
		thresh.setGrey_v_high(this.grey_v_high / 100.0);

		// Green Plates
		thresh.setGreen_r_low(this.green_r_low);
		thresh.setGreen_r_high(this.green_r_high);
		thresh.setGreen_g_low(this.green_g_low);
		thresh.setGreen_g_high(this.green_g_high);
		thresh.setGreen_b_low(this.green_b_low);
		thresh.setGreen_b_high(this.green_b_high);
		thresh.setGreen_h_low(this.green_h_low / 100.0);
		thresh.setGreen_h_high(this.green_h_high / 100.0);
		thresh.setGreen_s_low(this.green_s_low / 100.0);
		thresh.setGreen_s_high(this.green_s_high / 100.0);
		thresh.setGreen_v_low(this.green_v_low / 100.0);
		thresh.setGreen_v_high(this.green_v_high / 100.0);

		// TODO: test
		// Goal locations
		WorldState.leftGoalTop = leftGoalTop;
		WorldState.leftGoalBottom = leftGoalBottom;
		WorldState.rightGoalTop = rightGoalTop;
		WorldState.rightGoalBottom = rightGoalBottom;
		WorldState.leftGoalCentre = leftGoalCentre;
		WorldState.rightGoalCentre = rightGoalCentre;

	}

	/**
	 * Loads default values for the constants, used when loading from a file
	 * fails.
	 */
	public void loadDefaultConstants() {

		/* Ball */
		this.ball_r_low = 0;
		this.ball_r_high = 255;
		this.ball_g_low = 0;
		this.ball_g_high = 255;
		this.ball_b_low = 0;
		this.ball_b_high = 255;
		this.ball_h_low = 0;
		this.ball_h_high = 10;
		this.ball_s_low = 0;
		this.ball_s_high = 10;
		this.ball_v_low = 0;
		this.ball_v_high = 10;

		/* Blue Robot */
		this.blue_r_low = 0;
		this.blue_r_high = 255;
		this.blue_g_low = 0;
		this.blue_g_high = 255;
		this.blue_b_low = 0;
		this.blue_b_high = 255;
		this.blue_h_low = 0;
		this.blue_h_high = 10;
		this.blue_s_low = 0;
		this.blue_s_high = 10;
		this.blue_v_low = 0;
		this.blue_v_high = 10;

		/* Yellow Robot */
		this.yellow_r_low = 0;
		this.yellow_r_high = 255;
		this.yellow_g_low = 0;
		this.yellow_g_high = 255;
		this.yellow_b_low = 0;
		this.yellow_b_high = 255;
		this.yellow_h_low = 0;
		this.yellow_h_high = 10;
		this.yellow_s_low = 0;
		this.yellow_s_high = 10;
		this.yellow_v_low = 0;
		this.yellow_v_high = 10;

		/* Grey Circles */
		this.grey_r_low = 0;
		this.grey_r_high = 255;
		this.grey_g_low = 0;
		this.grey_g_high = 255;
		this.grey_b_low = 0;
		this.grey_b_high = 255;
		this.grey_h_low = 0;
		this.grey_h_high = 10;
		this.grey_s_low = 0;
		this.grey_s_high = 10;
		this.grey_v_low = 0;
		this.grey_v_high = 10;

		/* Green plates */
		this.green_r_low = 0;
		this.green_r_high = 255;
		this.green_g_low = 0;
		this.green_g_high = 255;
		this.green_b_low = 0;
		this.green_b_high = 255;
		this.green_h_low = 0;
		this.green_h_high = 10;
		this.green_s_low = 0;
		this.green_s_high = 10;
		this.green_v_low = 0;
		this.green_v_high = 10;

		/* Pitch Dimensions */
		this.topBuffer = 0;
		this.bottomBuffer = 0;
		this.leftBuffer = 0;
		this.rightBuffer = 0;

		// TODO: those are random; put real values
		// goal locations
		this.leftGoalTop = new Point2(55, 142);
		this.leftGoalBottom = new Point2(52, 286);
		this.leftGoalCentre = new Point2(54, 214);
		this.rightGoalTop = new Point2(572, 148);
		this.rightGoalBottom = new Point2(577, 287);
		this.rightGoalCentre = new Point2(574, 217);
	}

}