package sdp.nxt;

import sdp.nxt.interfaces.impl.regulatedchassis.DiffPilot;
import sdp.nxt.interfaces.impl.regulatedchassis.KickerOnRegulatedChassis;

/**
 * This should be run on brick B as long as it is attached to the same chassis
 * with regulated motors.
 * 
 * @author s1117764
 * 
 */
public class StartRegulated {
	public static void main(String args[]) {
		new BluetoothReciever(new DiffPilot(), new KickerOnRegulatedChassis(
				true)).connect();
	}
}
