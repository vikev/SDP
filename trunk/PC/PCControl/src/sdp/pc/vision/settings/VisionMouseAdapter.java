package sdp.pc.vision.settings;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;

/**
 * The mouse click adapter which is used for setting raw boundaries.
 */
public class VisionMouseAdapter extends MouseAdapter {
	public static Point lastClickPos;

	@Override
	public void mousePressed(MouseEvent e) {
		
		// Get the click
		lastClickPos = Vision.frame.getContentPane().getMousePosition();
		
		// if the click is unknown ignore it
		if (lastClickPos == null) {
			return;
		}
		
		Point2 p2 = new Point2(lastClickPos);
		
		// If the system is waiting for a boundary set it
		if (!SettingsManager.defaultSettings.hasBoundary()) {
			SettingsManager.defaultSettings
					.addBoundary(new Point2(lastClickPos));
			System.out.println("Boundary added [" + lastClickPos.x + ", "
					+ lastClickPos.y + "]");
			if (SettingsManager.defaultSettings.hasBoundary())
				Vision.stateListener.forcePreprocess();
			return;
		}
		
		// Otherwise request data from Vision.
		Vision.setRequestedPoint(p2);
	}
}
