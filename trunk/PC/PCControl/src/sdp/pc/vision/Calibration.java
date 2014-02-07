package sdp.pc.vision;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;

public class Calibration extends MouseAdapter {
	public static Point lastClickPos;

	@Override
	public void mousePressed(MouseEvent e) {
		lastClickPos = Vision.frame.getContentPane().getMousePosition();
		
		if (lastClickPos != null && !WorldStateListener.hasBoundary()) {
			WorldStateListener.addBoundary(new Point2(lastClickPos));
			System.out.println("Boundary added [" + lastClickPos.x + ", " + lastClickPos.y + "]");
		}
	}
}
