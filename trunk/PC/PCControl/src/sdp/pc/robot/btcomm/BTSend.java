package sdp.pc.robot.btcomm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;
import lejos.pc.comm.NXTInfo;

public class BTSend {
	static void connect(NXTInfo info, int mode) {
		NXTConnector conn = new NXTConnector();

		conn.addLogListener(new NXTCommLogListener() {

			public void logEvent(String message) {
				System.out.println("BTSend Log.listener: " + message);

			}

			public void logEvent(Throwable throwable) {
				System.out.println("BTSend Log.listener - stack trace: ");
				throwable.printStackTrace();

			}

		});
		// Connect to any NXT over Bluetooth
		boolean connected = conn.connectTo(info, mode);

		if (!connected) {
			System.err.println("Failed to connect to any NXT");
			System.exit(1);
		}

		DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
		DataInputStream dis = new DataInputStream(conn.getInputStream());
		boolean run = true;
		Scanner input = new Scanner(System.in);
		while (run) {
			String c = input.next();
			if ("exit".equalsIgnoreCase(c)) {
				run = false;
			} else {
				try {
					dos.writeChar(c.charAt(0));
					dos.writeInt(c.length());
					dos.flush();

				} catch (IOException ioe) {
					System.out.println("IO Exception writing bytes:");
					System.out.println(ioe.getMessage());
				}

				try {
					System.out.println("Received " + dis.readChar());
					System.out.println("Received " + dis.readInt());
				} catch (IOException ioe) {
					System.out.println("IO Exception reading bytes:");
					System.out.println(ioe.getMessage());
				}
			}
		}
		
		try {
			dis.close();
			dos.close();
			conn.close();
		} catch (IOException ioe) {
			System.out.println("IOException closing connection:");
			System.out.println(ioe.getMessage());
		}
	}
}
