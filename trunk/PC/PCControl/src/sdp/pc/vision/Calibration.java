package sdp.pc.vision;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;

import sdp.pc.common.Constants;

public class Calibration extends MouseAdapter {
	public static Point pos2;

	@Override
	public void mousePressed(MouseEvent e) {
		pos2 = Vision.frame.getMousePosition();
		if (pos2 != null) {
			int x = (int) Math.round(pos2.getX()) - Constants.X_FRAME_OFFSET;
			int y = (int) Math.round(pos2.getY()) - Constants.Y_FRAME_OFFSET;

			if (Vision.leftTop.getX() == 0 && Vision.leftTop.getY() == 0) {
				Vision.leftTop = new Point2(x, y);
				System.out.println("Select lower-right-most boundary point");
			} else if (Vision.rightBottom.getX() == 0
					&& Vision.rightBottom.getY() == 0) {
				Vision.rightBottom = new Point2(x, y);
				Vision.edgesCalibrated = true;
			} else {
				Vision.requestedData = new Point2(x, y);
			}

		}
	}
}
