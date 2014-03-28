package sdp.nxt.bt;

import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import sdp.nxt.interfaces.Kicker;
import sdp.nxt.interfaces.Pilot;

public class BT {

	private Pilot pilot;
	private Kicker kicker;

	private final String CONNECTED = "Connected";
	private final String WAITING = "Waiting...";

	public BT(Pilot pilot, Kicker kicker) {
		this.pilot = pilot;
		this.kicker = kicker;
	}

	private int byteArrayToShortAsInt(byte[] data, int offset) {
		return (int) (((data[offset] << 8)) | ((data[offset + 1] & 0xff)));
	}

	public void connect() {
		while (true) {
			try {
				LCD.clear();
				LCD.drawString(WAITING, 0, 0);
				LCD.refresh();
				BTConnection btc = Bluetooth.waitForConnection();
				LCD.clear();
				LCD.drawString(CONNECTED, 0, 0);
				LCD.refresh();

				byte[] data = new byte[3];
				while (btc.read(data, 3) == 3) {

					byte c = data[0];
					int power = byteArrayToShortAsInt(data, 1);
					System.out.println(c + " " + power);
					switch (c) {
					case 1:
						pilot.forward(power);
						break;
					case 2:
						pilot.backward(power);
						break;
					case 3:
						pilot.turnLeft(power);
						break;
					case 4:
						pilot.turnRight(power);
						break;
					case 5:
						pilot.stop(); // stop robot immediately
						break;
					case 6:
						kicker.kick(power);
						break;
					case 7:
						kicker.grab();
						break;
					case 8:
						pilot.setPower(power);
					case 9:
						kicker.open();
					}
				}
				// stop if connection is lost
				pilot.stop();
			} catch (NullPointerException e) {
				pilot.stop();
			}
		}
	}
}
