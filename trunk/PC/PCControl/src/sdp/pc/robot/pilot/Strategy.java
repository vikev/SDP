package sdp.pc.robot.pilot;

/**
 * @author s1133141
 */

import sdp.pc.common.ChooseRobot;
import sdp.pc.vision.*;
import sdp.pc.vision.relay.Driver;
import sdp.pc.vision.relay.TCPClient;

public class Strategy extends WorldState{
	
	public Strategy() throws Exception
	{
		Driver driver = new Driver(new TCPClient(ChooseRobot.dialog()));
	}
	
	public void robotFallen(int pilot)
	{
		if ()
	}

}
