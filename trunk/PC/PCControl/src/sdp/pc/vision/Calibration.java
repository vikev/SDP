package sdp.pc.vision;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;

public class Calibration extends MouseAdapter {
	public static Point pos;
	
	public final WorldStateListener stateListener;
	
	public Calibration(WorldStateListener stateListener) {
		this.stateListener = stateListener;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		pos = Vision.frame.getContentPane().getMousePosition();
		
		if (pos != null) {
			if(stateListener != null && !stateListener.hasBoundary()) {
				
				stateListener.addBoundary(new Point2(pos));
				System.out.println("Boundary added [" + pos.x + ", " + pos.y + "]");
			}
		}
	}
}
