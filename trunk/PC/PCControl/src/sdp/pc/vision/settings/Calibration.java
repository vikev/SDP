package sdp.pc.vision.settings;

import static sdp.pc.vision.settings.SettingsManager.defaultSettings;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;

/**
 * Class which extends a mouse adapter and is used in combination with the
 * control gui for selecting boundaries. It once supported selecting arbitrary
 * points to print rgb/hsb data but the class has become extremely disorganized.
 * 
 * @author s1143704
 * 
 */
public class Calibration extends MouseAdapter {

	/**
	 * A referenceable point for the last click position
	 */
	public static Point lastClickPos;

	/**
	 * The inherited method for click listening
	 */
	@Override
	public void mousePressed(MouseEvent e) {

		/**
		 * Overwrite the last click position
		 */
		lastClickPos = Vision.frame.getContentPane().getMousePosition();

		// Add a new boundary, if boundaries aren't set.
		if (lastClickPos != null && !defaultSettings.hasBoundary()) {
			defaultSettings.addBoundary(new Point2(lastClickPos));
			
			System.out.println("Boundary added [" + lastClickPos.x + ", "
					+ lastClickPos.y + "]");

			return;
		}

		// Set the left goal top, if it is unset.
		if (lastClickPos != null && WorldState.leftGoalTop == null) {
			WorldState.leftGoalTop = new Point2(lastClickPos);
			System.out.println("Top left goal added [" + lastClickPos.x + ", "
					+ lastClickPos.y + "]");
			return;
		}

		// Set the left goal bottom, if it is unset.
		if (lastClickPos != null && WorldState.leftGoalBottom == null) {
			WorldState.leftGoalBottom = new Point2(lastClickPos);
			System.out.println("Bottom left goal added [" + lastClickPos.x
					+ ", " + lastClickPos.y + "]");
			return;
		}

		// Set the right goal top, if it is unset.
		if (lastClickPos != null && WorldState.rightGoalTop == null) {
			WorldState.rightGoalTop = new Point2(lastClickPos);
			System.out.println("Top right goal added [" + lastClickPos.x + ", "
					+ lastClickPos.y + "]");
			return;
		}

		// Set the right goal bottom, if it is unset.
		if (lastClickPos != null && WorldState.rightGoalBottom == null) {
			WorldState.rightGoalBottom = new Point2(lastClickPos);
			System.out.println("Bottom right goal added [" + lastClickPos.x
					+ ", " + lastClickPos.y + "]");
			return;
		}

		// TODO: If the client wasn't setting a boundary, they were requesting
		// static image data (normalised rgb/hsb values) - here we used to
		// display hsb/rgb data, but maybe now we should simply print
		// lastClickPos data in the world state classes?
	}
}
