package sdp.nxj.bluetooth;

import java.io.DataInputStream;

import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

/**
 * Receive data from another NXT, a PC, a phone, or another bluetooth device.
 * 
 * Waits for a connection, receives an int and returns its negative as a reply,
 * 100 times, and then closes the connection, and waits for a new one.
 * 
 * @author Lawrie Griffiths
 * 
 */
public class BTReceive {

	public static void main(String[] args) throws Exception {
		try {
			DiffPilot pilot = new DiffPilot();

			String connected = "Connected";
			String waiting = "Waiting...";
			String closing = "Closing...";

			Kicker kicker = null;
			KickerAtt kickerAtt = null;

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
						double speed = dis.readDouble();
						// if the speed is negative reverse the direction of
						// movement
						if (speed < 0) {
							speed = -speed;
							switch (c) {
							case 'f':
								c = 'b';
								break;
							case 'b':
								c = 'f';
								break;
							case 'l':
								c = 'r';
								break;
							case 'r':
								c = 'l';
								break;
							}
						}

						// dos.writeChar(c);
						// dos.writeDouble(speed);
						// dos.flush();
						// speed is the rotation speed in degrees per second
						pilot.setRotateSpeed(speed);
						switch (c) {
						case 'f':
							pilot.driveForward(0);
							break;
						case 'b':
							pilot.driveBackward(0);
							break;
						case 'l':
							pilot.turnLeft(0);
							break;
						case 'r':
							pilot.turnRight(0);
							break;
						case 's':
							pilot.stopNow(); // stop robot immediately
							break;
						case 'k':
							if (kicker == null || !kicker.isAlive()) {
								kicker = new Kicker(speed);
								kicker.setDaemon(true);
								kicker.run();
							}
							break;
						case 'g':
							if (kicker == null || !kicker.isAlive()) {
								kicker = new Kicker(speed);
								kicker.setDaemon(true);
								kicker.grab();
							}
							break;
						case 'p':
							if (kickerAtt == null || !kickerAtt.isAlive()) {
								kickerAtt = new KickerAtt(speed);
								kickerAtt.setDaemon(true);
								kickerAtt.start();
							}
							break;
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
				pilot.stopNow();
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
