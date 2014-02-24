package sdp.nxt;

import java.io.DataInputStream;

import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import sdp.nxt.interfaces.Kicker;
import sdp.nxt.interfaces.Pilot;

public class BluetoothReciever {

	Pilot pilot;
	Kicker kicker;

	public BluetoothReciever(Pilot pilot, Kicker kicker) {
		this.pilot = pilot;
		this.kicker = kicker;
	}

	public void connect() {
		try {
			String connected = "Connected";
			String waiting = "Waiting...";
			String closing = "Closing...";

			while (true) {
				LCD.drawString(waiting, 0, 0);
				LCD.refresh();

				BTConnection btc = Bluetooth.waitForConnection();

				LCD.clear();
				LCD.drawString(connected, 0, 0);
				LCD.refresh();

				DataInputStream dis = btc.openDataInputStream();
				// DataOutputStream dos = btc.openDataOutputStream();

				while (true) {
					try {
						char c = dis.readChar();
						int power = dis.readInt();
						// if the power is negative reverse the direction of
						// movement
						// if (power < 0) {
						// power = -power;
						// switch (c) {
						// case 'f':
						// c = 'b';
						// break;
						// case 'b':
						// c = 'f';
						// break;
						// case 'l':
						// c = 'r';
						// break;
						// case 'r':
						// c = 'l';
						// break;
						// }
						// }

						// dos.writeChar(c);
						// dos.writeDouble(power);
						// dos.flush();
						// power is the rotation power in degrees per second
						pilot.setPower((int) power);
						switch (c) {
						case 'f':
							pilot.forward(power);
							break;
						case 'b':
							pilot.backward(power);
							break;
						case 'l':
							pilot.turnLeft(power);
							break;
						case 'r':
							pilot.turnRight(power);
							break;
						case 's':
							pilot.stop(); // stop robot immediately
							break;
						case 'k':
							kicker.kick((int) power);
							break;
						case 'g':
							kicker.grab();
							break;
						case 'p':
							pilot.setPower(power);
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
						break;
					}

				}
				try {
					dis.close();
					// dos.close();
				} catch (Exception e) {
					// nothing
				}
				pilot.stop();
				Thread.sleep(100); // wait for data to drain
				// LCD.clear();
				LCD.drawString(closing, 0, 0);
				LCD.refresh();
				btc.close();
				LCD.clear();
			}
		} catch (Exception e) {
			System.out.println("Don't care. Restart.");
		}
	}
}
