package sdp.pc.vision;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import sdp.pc.common.Constants;

/**
 * Takes care of painting a {@link WorldState} overlay atop a
 * {@link BufferedImage}. Supports highlighting specified colour ranges as
 * defined in Colors.java (incurring visible FPS drop on DICE machines).
 * 
 * @author Ix
 */
public class WorldStatePainter {
	/**
	 * The minimum speed considered as moving TODO: move to WorldState?
	 */
	private static final double BALL_SPEED_THRESHOLD = 3.0;
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
	enum HighlightMode {
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
				return Color.YELLOW;
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
				return Color.BLACK;
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
				return Color.BLACK;
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
	 * 				the current cursor position used to print pixel data
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
			g.drawString(
					"Waiting for preprocessor... "
							+ stateListener.getKeyFrames() + "%", TEXT_OFFSET,
					TEXT_OFFSET);
			for (int ix = 0; ix < Vision.WIDTH; ix++)
				for (int iy = 0; iy < Vision.HEIGHT; iy++) {
					cRgb = new Color(image.getRGB(ix, iy));
					Color.RGBtoHSB(cRgb.getRed(), cRgb.getGreen(),
							cRgb.getBlue(), cHsb);
					if (Colors.isWhite(cRgb, cHsb))
						image.setRGB(ix, iy, Color.white.getRGB());
				}
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
		// draw ball if and only if the ball is on the table
		if (!ballPos.equals(Point2.EMPTY)) {
			g.setColor(Color.red);
			g.drawLine(0, ballPos.getY(), 640, ballPos.getY());
			g.drawLine(ballPos.getX(), 0, ballPos.getX(), 480);

			// draw ball velocity (if above the threshold)
			if (ballVelocity.length() > BALL_SPEED_THRESHOLD) {
				Point2 velocityPos = ballPos.add(ballVelocity);
				g.drawLine(ballPos.getX(), ballPos.getY(), velocityPos.getX(),
						velocityPos.getY());
			}
			
			// futureballs
			if(!state.getEstimatedStopPoint().equals(Point2.EMPTY)){
				drawCircle(g, Constants.GRAY_BLEND, FutureBall.estimateRealStopPoint(),
						Constants.ROBOT_HEAD_RADIUS);
				if(!state.getEstimatedCollidePoint().equals(Point2.EMPTY)){
					drawCircle(g, Constants.GRAY_BLEND, FutureBall.collision, 5);
				}
			}
		}

		// loop through all robots
		for (int team = 0; team < 2; team++)
			for (int robot = 0; robot < 2; robot++) {
				// robot position, facing and nose
				Point2 robotPos = state.getRobotPosition(team, robot);
				double robotFacing = state.getRobotFacing(team, robot);

				// draw robots, if and only if they are on the table
				if (!robotPos.equals(Point2.EMPTY)) {
					drawCircle(g, Constants.YELLOW_BLEND, robotPos,
							Constants.ROBOT_CIRCLE_RADIUS);
					if (!(robotFacing == Double.NaN)) {
						Point2 nosePos = robotPos.polarOffset(ROBOT_NOSE,
								robotFacing + 180);
						drawCircle(g, Color.WHITE, nosePos, 3);
						g.drawLine(robotPos.x, robotPos.y, nosePos.x, nosePos.y);
					}
				}
			}


		

		// draw centre line
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.3f));
		g.drawLine(Constants.TABLE_CENTRE_X, Constants.TABLE_MIN_Y + 1,
				Constants.TABLE_CENTRE_X, Constants.TABLE_MAX_Y - 1);

		// calculate/get the different FPS
		long nowRun = System.currentTimeMillis();
		int drawFps = (int) (1000 / (nowRun - lastRun));
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
		if (mousePos != null) {

			
			
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
				
				//get attacker/defender zone
				String sAtkDef = "";
				Pitch pitch = state.getPitch();
				if(pitch.isPointInAttackerZone(mousePos))
					sAtkDef += " (A)";
				if(pitch.isPointInDefenderZone(mousePos))
					sAtkDef += " (D)";
				
				//get position, rgb, hsb
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

		// bin your litter
		g.dispose();
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
}
