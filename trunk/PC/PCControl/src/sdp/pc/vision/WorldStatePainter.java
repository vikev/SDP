package sdp.pc.vision;

import static sdp.pc.vision.settings.SettingsManager.defaultSettings;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import sdp.pc.common.GaussianIntFilter;

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
	 * The most recent requested data point which we draw a circle around.
	 */
	private Point2 lastPoint = Point2.EMPTY;

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
	 * A filter for the observed world FPS. Size should roughly account for 1s of time. 
	 */
	private GaussianIntFilter worldFpsFilter = new GaussianIntFilter(24);
	/**
	 * A filter for the observed clock FPS. Size should roughly account for 1s of time. 
	 */
	private GaussianIntFilter clockFpsFilter = new GaussianIntFilter(24);

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
	 * Primitive method for printing out a float array
	 * 
	 * @param s
	 * @return
	 */
	private static String floatArrayToString(float[] s) {
		return "(" + s[0] + ", " + s[1] + ", " + s[2] + ")";
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

		// Draw the most recently requested data
		Point2 q = Vision.getRequestedPoint();
		if (!q.equals(Point2.EMPTY)
				&& Vision.getWorldState().getPitch().contains(q)) {
			lastPoint = q;
			String hsb = floatArrayToString(stateListener.getNormalisedHsb(
					lastPoint.x, lastPoint.y));
			String rgb = stateListener.getNormalisedRgb(lastPoint.x,
					lastPoint.y).toString();
			System.out.println("\nRequested Data: \nNormalised HSB: " + hsb
					+ "\nNormalised RGB: " + rgb);
			System.out.println("Quadrant: "+state.quadrantFromPoint(q));
		}
		if (!lastPoint.equals(Point2.EMPTY)) {
			drawCircle(g, GRAY_BLEND, lastPoint, 3);
		}

		// ball location and velocity
		Point2 ballPos = state.getBallPosition();
		Point2 ballVelocity = state.getBallVelocity();
		ballVelocity = ballVelocity.mult(-1);
		double ballSpeed = state.getBallSpeed();
		// draw ball if and only if the ball is on the table
		if (!ballPos.equals(Point2.EMPTY)) {
			g.setColor(RED_BLEND_MORE);
			g.drawLine(0, ballPos.getY(), 640, ballPos.getY());
			g.drawLine(ballPos.getX(), 0, ballPos.getX(), 480);

			// draw ball velocity (if above the threshold)
			if (ballSpeed > BALL_SPEED_THRESHOLD) {
				g.setColor(RED_BLEND);
				Point2 velocityPos = ballPos.add(ballVelocity);
				g.drawLine(ballPos.getX(), ballPos.getY(), velocityPos.getX(),
						velocityPos.getY());
			}

			Intersect data = state.getFutureData();

			Point2 last = state.getBallPosition();

			// FutureBall data
			for (int i = 0; i < data.getIntersections().size(); i++) {
				drawLine(g, Color.WHITE, last, data.getIntersections().get(i));
				last = data.getIntersections().get(i);
				drawCircle(g, Color.BLUE, last, 3);
			}

			drawLine(g, Color.WHITE, last, data.getEstimate());
			drawCircle(g, Color.BLUE, data.getEstimate(), 3);
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

		// drawCircle(g, Color.WHITE, state.getPitch().pitchCornerX, 1);
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

		worldFps = worldFpsFilter.apply(worldFps);
		clockFps = clockFpsFilter.apply(clockFps);
		

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
			ArrayList<Point2> points = pitch.getArrayListOfPoints();

			// Draw lines between all border panels
			drawLine(g, Color.WHITE, points.get(1), points.get(2));
			drawLine(g, Color.WHITE, points.get(2), points.get(3));
			drawLine(g, Color.WHITE, points.get(3), points.get(4));
			drawLine(g, Color.WHITE, points.get(4), points.get(5));
			drawLine(g, Color.WHITE, points.get(5), points.get(6));
			drawLine(g, Color.WHITE, points.get(6), points.get(7));
			drawLine(g, Color.WHITE, points.get(7), points.get(8));
			drawLine(g, Color.WHITE, points.get(8), points.get(9));
			drawLine(g, Color.WHITE, points.get(9), points.get(10));
			drawLine(g, Color.WHITE, points.get(10), points.get(11));
			drawLine(g, Color.WHITE, points.get(11), points.get(12));
			drawLine(g, Color.WHITE, points.get(12), points.get(13));
			drawLine(g, Color.WHITE, points.get(13), points.get(14));
			drawLine(g, Color.WHITE, points.get(14), points.get(1));

			// Draw circles around every pitch vertex
			for (Point2 point : points) {
				drawCircle(g, Color.BLUE, point, 2);
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
	 * Draws a line between points
	 * 
	 * @param g
	 *            - Graphics object
	 * @param c
	 *            - Color of the line
	 * @param a
	 *            - From point...
	 * @param b
	 *            - ...to point
	 */
	private void drawLine(Graphics g, Color c, Point2 a, Point2 b) {
		g.setColor(c);
		g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
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
