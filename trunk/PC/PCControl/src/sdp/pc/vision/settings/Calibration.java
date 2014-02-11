package sdp.pc.vision.settings;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.WorldStateListener;

public class Calibration extends MouseAdapter {
	public static Point lastClickPos;

	@Override
	public void mousePressed(MouseEvent e) {
		lastClickPos = Vision.frame.getContentPane().getMousePosition();

		if (lastClickPos != null && !WorldStateListener.hasBoundary()) {
			WorldStateListener.addBoundary(new Point2(lastClickPos));
			System.out.println("Boundary added [" + lastClickPos.x + ", "
					+ lastClickPos.y + "]");
			if (WorldStateListener.hasBoundary()) {
				Vision.stateListener.setPreprocessed(false);
			}
			return;
		}
		if (lastClickPos != null && WorldState.leftGoalTop == null) {
			WorldState.leftGoalTop = new Point2(lastClickPos);
			System.out.println("Top left goal added [" + lastClickPos.x + ", "
					+ lastClickPos.y + "]");
			return;
		}
		if (lastClickPos != null && WorldState.leftGoalBottom == null) {
			WorldState.leftGoalBottom = new Point2(lastClickPos);
			System.out.println("Bottom left goal added [" + lastClickPos.x
					+ ", " + lastClickPos.y + "]");
			return;
		}
		if (lastClickPos != null && WorldState.rightGoalTop == null) {
			WorldState.rightGoalTop = new Point2(lastClickPos);
			System.out.println("Top right goal added [" + lastClickPos.x + ", "
					+ lastClickPos.y + "]");
			return;
		}
		if (lastClickPos != null && WorldState.rightGoalBottom == null) {
			WorldState.rightGoalBottom = new Point2(lastClickPos);
			System.out.println("Bottom right goal added [" + lastClickPos.x
					+ ", " + lastClickPos.y + "]");
			return;
		}
		// If the client wasn't setting a boundary, they were requesting
		// static image data (normalised rgb/hsb values)
	}
}
