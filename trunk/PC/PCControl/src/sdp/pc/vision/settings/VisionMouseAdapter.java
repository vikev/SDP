package sdp.pc.vision.settings;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import sdp.pc.vision.Point2;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldStatePainter;
import sdp.pc.vision.WorldStatePainter.HighlightMode;

/**
 * The mouse click adapter which is used for setting raw boundaries.
 */
public class VisionMouseAdapter extends MouseAdapter {
	public static Point lastClickPos;

	private static Point2 getMousePosition() {

		// Get the click
		Point p = Vision.frame.getContentPane().getMousePosition();
		
		// scale size in the range [WIDTH; HEIGHT]
		p.x = p.x * Vision.WIDTH / Vision.frameLabel.getWidth();
		p.y = p.y * Vision.HEIGHT / Vision.frameLabel.getHeight();
		
		
		return new Point2(p);
	}
	
	@Override
    public void mouseWheelMoved(MouseWheelEvent e) {
		int cId = Vision.statePainter.getColorSettingId();
		if(cId != -1) {			
			int[] min = SettingsManager.defaultSettings.getMinValues(cId);
			min[3] -= e.getWheelRotation();
			Vision.settingsGui.sliderPanel.update();
		}
	}
	
	
	@Override
	public void mousePressed(MouseEvent e) {
		Point2 p = getMousePosition();
		
		// If the system is waiting for a boundary set it
		if (!SettingsManager.defaultSettings.hasBoundary()) {
			SettingsManager.defaultSettings.addBoundary(p);
			
			System.out.printf("Boundary added [%d, %d]\n", p.x, p.y);
			
			if (SettingsManager.defaultSettings.hasBoundary())
				Vision.stateListener.forcePreprocess();
			
			return;
		}
		
		//check if we have highlighted a color
		int cId = Vision.statePainter.getColorSettingId();
		if(cId != -1) {
			Color cRgb = Vision.stateListener.getNormalisedRgb(p.x, p.y);
			float[] cHsb = Vision.stateListener.getNormalisedHsb(p.x, p.y);
			
			int[] min = SettingsManager.defaultSettings.getMinValues(cId);
			int[] max = SettingsManager.defaultSettings.getMaxValues(cId);
			
			if(SettingsManager.defaultSettings.isUseAltColors()) {
				min[0] = cRgb.getRed();
				min[1] = cRgb.getGreen();
				min[2] = cRgb.getBlue();
			}
			else {
				min[0] = Math.max(0, cRgb.getRed()	 	- 10);
				min[1] = Math.max(0, cRgb.getGreen() 	- 10);
				min[2] = Math.max(0, cRgb.getBlue()	 	- 10);
				
				max[0] = Math.min(255, cRgb.getRed()	+ 10);
				max[1] = Math.min(255, cRgb.getGreen()	+ 10);
				max[2] = Math.min(255, cRgb.getBlue()	+ 10);
				
				for(int i = 0; i < 3; i++) {
					min[3 + i] = Math.max(0, (int)(cHsb[0] * 100) - 10);
					max[3 + i] = Math.min(100, (int)(cHsb[0] * 100) + 10);
				}
				
			}
			Vision.settingsGui.sliderPanel.update();
			
			return;
		}
		
		
		// Otherwise request data from Vision.
		Vision.setRequestedPoint(p);
	}
}
