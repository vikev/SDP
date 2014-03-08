package sdp.nxt;

import sdp.nxt.interfaces.impl.multiplexer.DiffPilotMultiplexer;
import sdp.nxt.interfaces.impl.multiplexer.KickerMultiplexer;

/**
 * This should be run on brick A as long as it is attached to the same chasi
 * with DC motors.
 */
public class StartMultiplexer {

	public static void main(String args[]) {
		new BluetoothReciever(new DiffPilotMultiplexer(),
				new KickerMultiplexer(true)).connect();
	}
}
