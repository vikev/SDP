package sdp.pc;

import sdp.pc.robot.btcomm.BTConnection;
import sdp.pc.robot.pilot.Driver;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class Start {
	public static void main(String[] args) {
		NXTInfo nxt1 = new NXTInfo(NXTCommFactory.BLUETOOTH, "SDP 9B",
				"001653077531");

//		 NXTInfo nxt1 = new NXTInfo(NXTCommFactory.BLUETOOTH, "SDP 9A",
//					"0016530BB5A3");

		BTConnection conn1 = new BTConnection(nxt1, NXTComm.PACKET);

		final Driver driver1 = new Driver(conn1);

		
		driver1.forward(50);
		driver1.turnLeft(90);
		driver1.forward(50);
		driver1.turnLeft(90);
		driver1.forward(50);
		driver1.turnLeft(90);
		driver1.forward(50);
		driver1.turnLeft(90);
		
		
		
		conn1.disconnect();
//		conn2.disconnect();

	}
}
