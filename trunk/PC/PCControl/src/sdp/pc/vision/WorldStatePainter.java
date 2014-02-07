package sdp.pc.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import sdp.pc.common.Constants;

public class WorldStatePainter {
	private static final double VECTOR_THRESHOLD = 3.0;
	private static final int ROBOT_NOSE = 10;
	
	private long lastRun; // For FPS calculation
	
	private HighlightColor highlightColor = HighlightColor.None;
	
	private WorldStateListener stateListener;
	private WorldState state;
	
	public WorldStatePainter(WorldStateListener stateListener, WorldState state) {
		this.stateListener = stateListener;
		this.state = state;
	}
	
	enum HighlightColor {
		None, White, Yellow, Blue, Green
	}

	private Color getHighlight(Color cRgb, float[] cHsb) {
		switch(highlightColor) {
		case White:
			if(Colors.isWhite(cRgb, cHsb))
				return Color.WHITE;
			return cRgb;
		case Yellow:
			if(Colors.isYellow(cRgb, cHsb))
				return Color.YELLOW;
			return cRgb;
		case Blue:
			if(Colors.isBlue(cRgb, cHsb))
				return Color.BLUE;
			return cRgb;
		case Green:
			if(Colors.isGreen(cRgb, cHsb))
				return Color.GREEN;
			return cRgb;
		}
		return cRgb;
	}

	/**
	 * Draws an overlay on the given image. 
	 * Should use this.state to draw objects of interest!
	 * Can also use this.stateListener to get normalised RGB/HSB values
	 * 
	 * @param image
	 *            the image to process
	 */
	public void drawWorld(BufferedImage image, Point2 mousePos) {
		
		Graphics g = image.getGraphics();
		
		float[] cHsb = new float[3];
		Color cRgb;

		//if pre-processing is not done just colour all 'white' pixels and return
		if(!stateListener.isPreprocessed()) {
			for(int ix = 0; ix < Vision.WIDTH; ix++) 
				for(int iy = 0; iy < Vision.HEIGHT; iy++) {
					cRgb = new Color(image.getRGB(ix, iy));
					Color.RGBtoHSB(cRgb.getRed(), cRgb.getGreen(), cRgb.getBlue(), cHsb);
					if(Colors.isWhite(cRgb, cHsb))
						image.setRGB(ix, iy, Color.white.getRGB());
				}
			return;
		}
		
		
		//draw highlight
		if(highlightColor != HighlightColor.None) {
			ArrayList<Point2> pitch = stateListener.getPitchPoints();
			for(Point2 p : pitch) {
				cRgb = stateListener.getNormalisedRgb(p.x, p.y);
				cHsb = stateListener.getNormalisedHsb(p.x, p.y);
				cRgb = getHighlight(cRgb, cHsb);
				image.setRGB(p.x, p.y, cRgb.getRGB());
			}
		}
		
		//ball location and velocity
		Point2 ballPos = state.getBallPosition();
		Point2 ballVelocity = state.getBallVelocity();
		ballVelocity = ballVelocity.mult(-5);
	
		g.setColor(Color.red);
		g.drawLine(0, ballPos.getY(), 640, ballPos.getY());
		g.drawLine(ballPos.getX(), 0, ballPos.getX(), 480);
		
		//draw velocity if large enough
		if (ballVelocity.length() > VECTOR_THRESHOLD) {
			Point2 velocityPos = ballPos.add(ballVelocity);
			g.drawLine(ballPos.getX(), ballPos.getY(),
					velocityPos.getX(), velocityPos.getY());
		}

		// robots locations
		for(int team = 0; team < 2; team++)
			for(int robot = 0; robot < 2; robot++) {
				
				Point2 robotPos = state.getRobotPosition(team, robot);
				double robotFacing = state.getRobotFacing(team, robot);
				Point2 nosePos = robotPos.polarOffset(ROBOT_NOSE, robotFacing);
				
				drawCircle(robotPos, g, Constants.YELLOW_BLEND,
						Constants.ROBOT_CIRCLE_RADIUS);
				g.drawLine(robotPos.x, robotPos.y, nosePos.x, nosePos.y);
			}

		// ???
//		if (Alg.pointInPitch(circlePt)) {
//			drawCircle(circlePt, g, Constants.GRAY_BLEND,
//					Constants.ROBOT_HEAD_RADIUS);
//		}

//		// ???
//		angSmoothingWriteIndex++;
//		angSmoothingWriteIndex %= ANGLE_SMOOTHING_FRAME_COUNT;

		// Draw centre line
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.3f));
		g.drawLine(Constants.TABLE_CENTRE_X,
				Constants.TABLE_MIN_Y + 1, Constants.TABLE_CENTRE_X,
				Constants.TABLE_MAX_Y - 1);

		// ???
//		Point2 dPt = new Point2(requestedData.getX(), requestedData.getY());
//		if (Alg.pointInPitch(dPt)) {
//			circlePt = requestedData.copy();
//			HighlightColor s = rgb[requestedData.getX()][requestedData.getY()];
//			System.out.println("RGB: " + s.getRed() + " " + s.getGreen() + " "
//					+ s.getBlue());
//			float[] h = hsb[requestedData.getX()][requestedData.getY()];
//			System.out.println("HSB: " + h[0] + " " + h[1] + " " + h[2]);
//			requestedData = new Point2(-1, -1);
//		}

		// Display the FPS we run at
		long nowRun = System.currentTimeMillis(); // Used to calculate the FPS.
		long drawFps = (1000 / (nowRun - lastRun));
		lastRun = nowRun;
		double worldFps = stateListener.getCurrentFps();
		
		g.setColor(Color.white);
		g.drawString("Draw FPS: " + (int) drawFps, 15, 15);
		g.drawString("World FPS: " + (int) worldFps, 15, 30);

		// Draw mouse position, RGB, and HSB values to screen
		
		if (mousePos != null && Alg.pointInPitch(mousePos)) {
			
			String colorTip = "normalised";
			cRgb = stateListener.getNormalisedRgb(mousePos.x, mousePos.y);
			cHsb = stateListener.getNormalisedHsb(mousePos.x, mousePos.y);
			
			//no normalised colours for this area
			// => use originals 
			if(cRgb == null) {
				cRgb = stateListener.getRgb(mousePos.x, mousePos.y);
				colorTip = "original";
			}
			
			//no colours at all? all good
			if(cRgb != null) {
				String strInfo = String.format("X: %d Y: %d", mousePos.x, mousePos.y);
				String strRgb = String.format("[%d, %d, %d] (RGB, %s)", cRgb.getRed(), cRgb.getGreen(), cRgb.getBlue(), colorTip);
				String strHsb = String.format("[%.2f, %.2f, %.2f] (HSB, %s)", cHsb[0], cHsb[1], cHsb[2], colorTip);

				g.drawString(strInfo, 15, 45);
				g.drawString(strRgb, 15, 60);
				g.drawString(strHsb, 15, 75);
			}
		}
		//dispose nicely
		g.dispose();
	}


	/**
	 * Abstraction of Graphics.drawOval which simplifies our circle drawing
	 */
	private void drawCircle(Point2 centrePt, Graphics gfx, Color c, int radius) {
		gfx.setColor(c);
		gfx.drawOval(centrePt.getX() - radius, centrePt.getY() - radius,
				radius * 2, radius * 2);
	}

	public void setHighlightColor(HighlightColor highlightColor) {
		this.highlightColor = highlightColor;
	}

	public HighlightColor getHighlightColor() {
		return highlightColor;
	}
}
