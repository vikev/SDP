package sdp.pc.robot.pilot;

/**
 * TODO: What is this class and why do we have it?
 * 
 * @author s1133141
 */

import sdp.pc.common.ChooseRobot;
import sdp.pc.vision.WorldState;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;

public class Strategy extends WorldState {

	Driver driver;

	public Strategy() throws Exception {
		driver = new Driver(new TCPClient(ChooseRobot.dialog()));
	}

	public void robotFallen(int pilot) {
		if (true) {

		}
	}

}
