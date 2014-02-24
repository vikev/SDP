package sdp.pc.vision;

import static sdp.pc.vision.settings.SettingsManager.defaultSettings;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static sdp.pc.common.Constants.*;

/**
 * Takes care of painting a {@link WorldState} overlay atop a
 * {@link BufferedImage}. Supports highlighting specified colour ranges as
 * defined in Colors.java (incurring visible FPS drop on DICE machines).
 * 
 * @author Ix
 */
public class WorldStatePainter {

	public ActionListener customPaintCode;

	/**
	 * Minimum ball speed for the robot to consider the ball as moving in pixels
	 * per second.
	 */
	private static final double BALL_SPEED_THRESHOLD = 50.0;
	/**
	 * The length of the nose of the robot exposing its current orientation
	 */
	private static final int ROBOT_NOSE = 10;

	/**
	 * The offset at which text is drawn from the (0,0) corner
	 */
	private static final int TEXT_OFFSET = 15;
	/**
	 * The offset at which each successive line is drawn
	 */
	private static final int TEXT_HEIGHT = 15;

	/**
	 * the timestamp of the last run; used for FPS calculation
	 */
	private long lastRun;

	/**
	 * the current highlighting mode
	 */
	private HighlightMode highlightMode = HighlightMode.None;

	/**
	 * The state listener we're attached to so we can use its normalised RGB/HSB
	 * data
	 */
	private WorldStateListener stateListener;
	/**
	 * The state we are attached to and paint using the drawWorld() method
	 */
	private WorldState state;

	/**
	 * Whether to display the rectangle with the raw boundaries
	 */
	private boolean rawBoundaryShown = false;

	/**
	 * Constructs a new WorldStatePainter given a WorldState and
	 * WorldStateListener to attach to
	 * 
	 * @param stateListener
	 *            the WorldStateListener to use for getting last frame's RGB/HSB
	 *            data
	 * @param state
	 *            the WorldState to attach and draw to
	 */
	public WorldStatePainter(WorldStateListener stateListener, WorldState state) {
		this.stateListener = stateListener;
		this.state = state;
	}

	/**
	 * An enumeration of all the highlight modes the WorldStatePainter supports
	 * New values should have appropriate code attached to the getHighlight()
	 * function
	 * 
	 * @author Ix
	 */
	public enum HighlightMode {
		None, Red, White, Yellow, Blue, Green, Black, All
	}

	/**
	 * Gets the colour that should be drawn on the image for the given RGB/HSB
	 * pair according to the current highlighting mode
	 * 
	 * @param cRgb
	 *            the RGB colour object of the pixel
	 * @param cHsb
	 *            the HSB colour values of the pixel
	 * @return the RGB colour object that should be drawn on the image
	 */
	private Color getHighlightColor(Color cRgb, float[] cHsb) {
		switch (highlightMode) {
		case White:
			if (Colors.isWhite(cRgb, cHsb))
				return Color.WHITE;
			return cRgb;
		case Red:
			if (Colors.isBall(cRgb, cHsb))
				return Color.RED;
			return cRgb;
		case Yellow:
			if (Colors.isYellow(cRgb, cHsb))
				return Color.BLUE;
			return cRgb;
		case Blue:
			if (Colors.isBlue(cRgb, cHsb))
				return Color.BLUE;
			return cRgb;
		case Green:
			if (Colors.isGreen(cRgb, cHsb))
				return Color.GREEN;
			return cRgb;
		case Black:
			if (Colors.isBlack(cRgb, cHsb))
				return Color.GRAY;
			return cRgb;
		case All:
			if (Colors.isWhite(cRgb, cHsb))
				return Color.WHITE;
			if (Colors.isYellow(cRgb, cHsb))
				return Color.YELLOW;
			if (Colors.isBlue(cRgb, cHsb))
				return Color.BLUE;
			if (Colors.isGreen(cRgb, cHsb))
				return Color.GREEN;
			if (Colors.isBlack(cRgb, cHsb))
				return Color.GRAY;
		default: // None or missing entry!
			return cRgb;
		}
	}

	/**
	 * Draws the world state overlay on the specified image using this.state and
	 * this.stateListener.
	 * <p />
	 * Currently implements:
	 * <ul>
	 * <li>White highlighting on missing boundary; otherwise highlights
	 * according to highlightMode</li>
	 * <li>draws the ball and its velocity, all robots and their facing</li>
	 * <li>overlay text showing last rendered image's (normalised) RGB/HSB
	 * values, mouse position, current FPS</li>
	 * <li>something else?</li>
	 * </ul>
	 * 
	 * @param image
	 *            the image to draw the overlay on
	 * @param mousePos
	 *            the current cursor position used to print pixel data
	 */
	public void drawWorld(BufferedImage image, Point2 mousePos) {
		Graphics g = image.getGraphics();
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		// HSB/RGB values used 'at the moment'
		float[] cHsb = new float[3];
		Color cRgb;

		// if pre-processing is not done yet just colour all 'white' pixels and
		// return
		if (!stateListener.isPreprocessed()) {
			drawPreprocessOverlay(image, g, mousePos);
			return;
		}

		// draw highlight, if any
		// considerably reduces FPS on DICE machines if other than None!
		if (highlightMode != HighlightMode.None) {
			Iterable<Point2> pitch = stateListener.getPitchPoints();
			for (Point2 p : pitch) {
				cRgb = stateListener.getNormalisedRgb(p.x, p.y);
				cHsb = stateListener.getNormalisedHsb(p.x, p.y);
				cRgb = getHighlightColor(cRgb, cHsb);
				if (p instanceof Point2 && cRgb instanceof Color) {
					image.setRGB(p.x, p.y, cRgb.getRGB());
				} else {
					System.out.println("Attemped to highlight null object");
				}
			}
		}

		// ball location and velocity
		Point2 ballPos = state.getBallPosition();
		Point2 ballVelocity = state.getBallVelocity();
		ballVelocity = ballVelocity.mult(-5);
		double ballSpeed = state.getBallSpeed();
		// draw ball if and only if the ball is on the table
		if (!ballPos.equals(Point2.EMPTY)) {
			g.setColor(RED_BLEND);
			g.drawLine(0, ballPos.getY(), 640, ballPos.getY());
			g.drawLine(ballPos.getX(), 0, ballPos.getX(), 480);

			// draw ball velocity (if above the threshold)
			if (ballSpeed > BALL_SPEED_THRESHOLD) {
				Point2 velocityPos = ballPos.add(ballVelocity);
				g.drawLine(ballPos.getX(), ballPos.getY(), velocityPos.getX(),
						velocityPos.getY());
			}

			Intersect data = state.getFutureData();

			// futureballs
			if (!data.getResult().equals(Point2.EMPTY)) {
				drawCircle(g, GRAY_BLEND, data.getResult(), ROBOT_HEAD_RADIUS);
			}

			// collision
			if (!data.getIntersection().equals(Point2.EMPTY)) {
				g.setColor(GRAY_BLEND);
				g.drawLine(state.getBallPosition().getX(), state
						.getBallPosition().getY(), data.getIntersection()
						.getX(), data.getIntersection().getY());
			}
		}

		// Loop through all robots
		for (int team = 0; team < 2; team++)
			for (int robot = 0; robot < 2; robot++) {
				// robot position, facing and nose
				Point2 robotPos = state.getRobotPosition(team, robot);
				double robotFacing = state.getRobotFacing(team, robot);

				// draw robots, if and only if they are on the table
				if (!robotPos.equals(Point2.EMPTY)) {
					drawCircle(g, YELLOW_BLEND, robotPos, ROBOT_CIRCLE_RADIUS);
					if (!(robotFacing == Double.NaN)) {
						Point2 nosePos = robotPos.polarOffset(ROBOT_NOSE,
								robotFacing + 180);
						drawCircle(g, Color.WHITE, nosePos, 3);
						g.drawLine(robotPos.x, robotPos.y, nosePos.x, nosePos.y);
					}
				}
			}


		/*
		//For Testing rebound code.
		Pitch pitch2 = state.getPitch();
		ArrayList<Point2> points2 = pitch2.getArrayListOfPoints();
		if (!points2.get(0).equals(new Point2(0,0))) {
		//if (1 ==2){
			Graphics h = image.getGraphics();
			h.setColor(BLUE_BLEND);
			
			Point2 inA = new Point2(369,322); // Ball
			Point2 hitA = new Point2(344,358); // collision
			double angleA = FutureBall.getOutwardAngle(inA, hitA);
			Point2 reboundA = FutureBall.getReboundPoint(inA,hitA,50,angleA);
			drawCircle(h, Color.RED, inA, 2);
			drawCircle(h, Color.RED, hitA, 2);		
			drawCircle(h, Color.WHITE, reboundA, 2);
		 
			Point2 inB = new Point2(273,117);
			Point2 hitB = new Point2(241,84);
			double angleB = FutureBall.getOutwardAngle(inB, hitB);
			Point2 reboundB = FutureBall.getReboundPoint(inB,hitB,50,angleB);
			drawCircle(h, Color.RED, inB, 2);
			drawCircle(h, Color.RED, hitB, 2);
			drawCircle(h, Color.RED, reboundB, 2);
			
			Point2 inC = new Point2(136,118);
			Point2 hitC = new Point2(80,110);
			double angleC = FutureBall.getOutwardAngle(inC, hitC);
			Point2 reboundC = FutureBall.getReboundPoint(inC,hitC,50,angleC);
			drawCircle(h, Color.RED, inC, 2);
			drawCircle(h, Color.RED, hitC, 2);
			drawCircle(h, Color.RED, reboundC, 2);
			
		}*/
		 
		
	
		//drawCircle(g, Color.WHITE, state.getPitch().pitchCornerX, 1);
		// draw centre line
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.3f));
		g.drawLine(TABLE_CENTRE_X, TABLE_MIN_Y + 1, TABLE_CENTRE_X,
				TABLE_MAX_Y - 1);

		// calculate/get the different FPS
		long nowRun = System.currentTimeMillis();
		int drawFps = (int) (1000 / (nowRun - lastRun));
		state.setPaintFps(drawFps);
		lastRun = nowRun;
		int worldFps = stateListener.getCurrentFps();
		int clockFps = stateListener.getClockFps();

		String sPaintFps = String.format("Paint: %2d", drawFps);
		String sWorldFps = String
				.format("World: %2d / %2d", worldFps, clockFps);

		// draw FPS
		g.setColor(Color.white);
		g.drawString(sPaintFps, TEXT_OFFSET, TEXT_OFFSET);
		g.drawString(sWorldFps, TEXT_OFFSET, TEXT_OFFSET + TEXT_HEIGHT);

		// display mouse position, RGB, and HSB values to screen (if any)
		if (mousePos != null && mousePos.x < Vision.WIDTH
				&& mousePos.y < Vision.HEIGHT) {

			String colorTip = "norm";
			cRgb = stateListener.getNormalisedRgb(mousePos.x, mousePos.y);
			cHsb = stateListener.getNormalisedHsb(mousePos.x, mousePos.y);

			// no normalised colours for this area
			// as it is not calculated for points outside the pitch
			// => use originals
			if (cRgb == null) {
				cRgb = stateListener.getRgb(mousePos.x, mousePos.y);
				colorTip = "orig";
			}

			// no colours at all? all good
			// ps. should it happen at all?
			if (cRgb != null) {

				// get attacker/defender zone
				String sAtkDef = "";
				Pitch pitch = state.getPitch();
				if (pitch.isPointInAttackerZone(mousePos))
					sAtkDef += " (A)";
				if (pitch.isPointInDefenderZone(mousePos))
					sAtkDef += " (D)";

				// get position, rgb, hsb
				String strPos = String.format("Pos: [%3d, %3d]", mousePos.x,
						mousePos.y) + sAtkDef;
				String strRgb = String.format("RGB: [%4d, %4d, %4d] (%s)",
						cRgb.getRed(), cRgb.getGreen(), cRgb.getBlue(),
						colorTip);
				String strHsb = String.format("HSB: [%.2f, %.2f, %.2f]",
						cHsb[0], cHsb[1], cHsb[2]);

				// draw text
				g.drawString(strRgb, TEXT_OFFSET, TEXT_OFFSET + 2 * TEXT_HEIGHT);
				g.drawString(strHsb, TEXT_OFFSET, TEXT_OFFSET + 3 * TEXT_HEIGHT);
				g.drawString(strPos, TEXT_OFFSET, TEXT_OFFSET + 4 * TEXT_HEIGHT);
			}
		}

		// pitch borders
		Pitch pitch = state.getPitch();
		if (pitch != null) {
			Graphics l = image.getGraphics();
			ArrayList<Point2> points = new ArrayList<Point2>();
			g.drawLine(pitch.goalLineX[0], pitch.goalLineY[0],
					pitch.goalLineX[0], pitch.goalLineY[1]);
			g.drawLine(pitch.goalLineX[1], pitch.goalLineY[0],
					pitch.goalLineX[1], pitch.goalLineY[1]);
			g.drawLine(pitch.pitchCornerX[0], pitch.pitchY[0],		
					pitch.pitchCornerX[1], pitch.pitchY[0]);
			g.drawLine(pitch.pitchCornerX[0], pitch.pitchY[1],
					pitch.pitchCornerX[1], pitch.pitchY[1]);
			g.drawLine(pitch.zoneX[0], pitch.pitchY[0], pitch.zoneX[0],
					pitch.pitchY[1]);
			g.drawLine(pitch.zoneX[1], pitch.pitchY[0], pitch.zoneX[1],
					pitch.pitchY[1]);
			g.drawLine(pitch.zoneX[2], pitch.pitchY[0], pitch.zoneX[2],
					pitch.pitchY[1]);
			g.drawLine(pitch.goalLineX[0], pitch.goalLineY[0],
					pitch.pitchCornerX[0], pitch.pitchY[0]);
			g.drawLine(pitch.goalLineX[0], pitch.goalLineY[1],
					pitch.pitchCornerX[0], pitch.pitchY[1]);
			g.drawLine(pitch.goalLineX[1], pitch.goalLineY[0],
					pitch.pitchCornerX[1], pitch.pitchY[0]);
			g.drawLine(pitch.goalLineX[1], pitch.goalLineY[1],
					pitch.pitchCornerX[1], pitch.pitchY[1]);
			points = pitch.getArrayListOfPoints();
			for (Point2 point : points){
				drawCircle(l,Color.BLUE,point,2);
			}
			
			
			
		}

		if (isRawBoundaryShown())
			drawRawBoundary(g);

		// bin your litter
		g.dispose();

		if (customPaintCode != null)
			customPaintCode.actionPerformed(new ActionEvent(image, 0, ""));

	}

	private void drawPreprocessOverlay(BufferedImage image, Graphics g,
			Point2 mousePos) {
		float[] cHsb = new float[3];
		Color cRgb;

		if (!defaultSettings.hasBoundary()) {
			g.drawString("Select raw boundary points", TEXT_OFFSET, TEXT_OFFSET);

			// draw white overlay to help selecting boundaries
			for (int ix = 0; ix < Vision.WIDTH; ix++)
				for (int iy = 0; iy < Vision.HEIGHT; iy++) {

					cRgb = new Color(image.getRGB(ix, iy));
					Color.RGBtoHSB(cRgb.getRed(), cRgb.getGreen(),
							cRgb.getBlue(), cHsb);

					if (Colors.isWhite(cRgb, cHsb))
						image.setRGB(ix, iy, Color.white.getRGB());
				}

			// draw text n background
			g.setColor(new Color(255, 127, 127, 127));
			if (defaultSettings.getBoundary(0).equals(Point2.EMPTY)) {
				// waiting for 1st point
				g.fillRect(0, 0, Vision.WIDTH / 2, Vision.HEIGHT / 2);

				g.setColor(Color.white);
				g.drawLine(mousePos.x, mousePos.y, Vision.WIDTH, mousePos.y);
				g.drawLine(mousePos.x, mousePos.y, mousePos.x, Vision.HEIGHT);
			} else {
				// waiting for 2nd point
				g.fillRect(Vision.WIDTH / 2, Vision.HEIGHT / 2,
						Vision.WIDTH / 2, Vision.HEIGHT / 2);

				g.setColor(Color.white);
				g.drawLine(mousePos.x, mousePos.y, 0, mousePos.y);
				g.drawLine(mousePos.x, mousePos.y, mousePos.x, 0);

			}
		} else {
			g.drawString(
					"Waiting for preprocessor... "
							+ stateListener.getKeyFrames() + "%", TEXT_OFFSET,
					TEXT_OFFSET);
		}
	}

	/**
	 * Draws the raw boundary of the world listener as a rectangle on the
	 * specified Graphics object.
	 * 
	 * @param g
	 *            The Graphics to draw on
	 */
	private void drawRawBoundary(Graphics g) {

		Point2 ptl = defaultSettings.getBoundary(0), plr = defaultSettings
				.getBoundary(1);

		g.setColor(Color.red);
		g.drawLine(ptl.x, ptl.y, ptl.x, plr.y);
		g.drawLine(ptl.x, ptl.y, plr.x, ptl.y);
		g.drawLine(plr.x, plr.y, ptl.x, plr.y);
		g.drawLine(plr.x, plr.y, plr.x, ptl.y);
	}

	/**
	 * Draws the outline of a circle with a given centre and radius
	 * 
	 * @param g
	 *            the graphics to draw on
	 * @param c
	 *            the colour of the drawn circle
	 * @param centrePt
	 *            the centre of the circle
	 * @param radius
	 *            the radius of the circle
	 */
	private void drawCircle(Graphics g, Color c, Point2 centrePt, int radius) {
		g.setColor(c);
		g.drawOval(centrePt.getX() - radius, centrePt.getY() - radius,
				radius * 2, radius * 2);
	}

	/**
	 * Sets the custom highlight mode overlaid on colours of interest; has no
	 * effect if pitch boundaries are unavailable as the overlay will be
	 * HighlightMode.White
	 */
	public void setHighlightMode(HighlightMode highlightMode) {
		this.highlightMode = highlightMode;
	}

	/**
	 * Gets the custom highlight mode for colours of interest; unspecified
	 * output if pitch boundaries are unavailable as the overlay will be
	 * HighlightMode.White
	 */
	public HighlightMode getHighlightMode() {
		return highlightMode;
	}

	public WorldState getWorldState() {
		return state;
	}

	public boolean isRawBoundaryShown() {
		return rawBoundaryShown;
	}

	public void setRawBoundaryShown(boolean rawBoundaryShown) {
		this.rawBoundaryShown = rawBoundaryShown;
	}
}
