package sdp.nxt;

import sdp.nxt.interfaces.impl.multiplexerb.DiffPilotBMultiplexer;
import sdp.nxt.interfaces.impl.multiplexerb.KickerBMultiplexer;

/**
 * This should be run on brick A as long as it is attached to the same chasi
 * with DC motors.
 */
public class BMultiplexer {

	public static void main(String args[]) {
		new BluetoothReciever(new DiffPilotBMultiplexer(),
				new KickerBMultiplexer(true)).connect();
	}
}
