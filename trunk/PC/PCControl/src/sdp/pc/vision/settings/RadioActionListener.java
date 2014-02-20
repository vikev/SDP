package sdp.pc.vision.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;

public abstract class RadioActionListener implements ActionListener {

	private JRadioButton lastButton;
	protected int lastButtonTag;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object src = e.getSource();
		boolean forced = e.getID() == 0;
		
		if(!(src instanceof JRadioButton))
			return;
		
		JRadioButton newButton = (JRadioButton) src;
		
		if(newButton == lastButton && !forced) {
			newButton.setSelected(true);
			return;
		}
		
		if(lastButton != null)
			lastButton.setSelected(false);
		
		newButton.setSelected(true);
		
		selectedChanged(newButton, e.getActionCommand(), forced);
		
		lastButton = newButton;
//		System.out.println("Updated: " + newButton.getText());
	}
	
	public void setSelectedButton(JRadioButton btn) {
		actionPerformed(new ActionEvent(btn, 0, btn.getActionCommand()));
	}
	
	public abstract void selectedChanged(JRadioButton newButton, String actionCommand, boolean forced);
}
