package sdp.nxt;

import sdp.nxt.bt.BT;
import sdp.nxt.interfaces.impl.multiplexerb.DiffPilotBMultiplexer;
import sdp.nxt.interfaces.impl.multiplexerb.KickerBMultiplexer;

/**
 * This should be run on brick A as long as it is attached to the same chassis
 * with DC motors.
 */
public class BBMultiplexer {

	public static void main(String args[]) {
		new BT(new DiffPilotBMultiplexer(), new KickerBMultiplexer(true))
				.connect();
	}
}
