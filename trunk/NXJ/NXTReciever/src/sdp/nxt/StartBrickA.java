package sdp.nxt;

import sdp.nxt.interfaces.impl.regulatedchassisA.DiffPilotA;
import sdp.nxt.interfaces.impl.regulatedchassisA.KickerOnRegulatedChassisA;

/**
 * This should be run on brick B as long as it is attached to the same chassis
 * with regulated motors.
 * 
 * @author s1117764
 * 
 */
public class StartBrickA {
	public static void main(String args[]) {
		new BluetoothReciever(new DiffPilotA(), new KickerOnRegulatedChassisA(
				true)).connect();
	}
}
