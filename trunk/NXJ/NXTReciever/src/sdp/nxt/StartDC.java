package sdp.nxt;

import sdp.nxt.interfaces.impl.dcchassis.DCPilot;
import sdp.nxt.interfaces.impl.dcchassis.KickerOnDCChassis;

/**
 * This should be run on brick A as long as it is attached to the same chasi
 * with DC motors.
 */
public class StartDC {

	public static void main(String args[]) {
		new BluetoothReciever(new DCPilot(), new KickerOnDCChassis(true))
				.connect();
	}
}
