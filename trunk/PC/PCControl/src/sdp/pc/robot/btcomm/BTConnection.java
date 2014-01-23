package sdp.pc.robot.btcomm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;
import lejos.pc.comm.NXTInfo;

public class BTConnection {
	boolean isConnected = false;
	NXTConnector conn;
	DataOutputStream dos; // = new DataOutputStream(conn.getOutputStream());
	DataInputStream dis; // = new DataInputStream(conn.getInputStream());

	NXTInfo info;

	/**
	 * Connect to a NXT
	 * 
	 * @param info
	 *            NXT connection details
	 * @param mode
	 *            Connection NXTComm constant
	 */
	public BTConnection(NXTInfo info, int mode) {
		this.info = info;
		conn = new NXTConnector();
		conn.addLogListener(new NXTCommLogListener() {

			public void logEvent(String message) {
				System.out.println("BTSend Log.listener: " + message);

			}

			public void logEvent(Throwable throwable) {
				System.out.println("BTSend Log.listener - stack trace: ");
				throwable.printStackTrace();

			}

		});

		while (!conn.connectTo(info, mode)) {
			System.out.println("Failed to connect to the NXT " + getDeviceName());
			System.out.println("Will try again...");
		}
		isConnected=true;
		dos = new DataOutputStream(conn.getOutputStream());
		dis = new DataInputStream(conn.getInputStream());
	}

	/**
	 * Get the name of the brick.
	 * 
	 * @return
	 */
	public String getDeviceName() {
		return info.name;
	}
	
	/**
	 * Check connection status.
	 * @return
	 */
	public boolean isConnected(){
		return isConnected;
	}

	/**
	 * Get the address of the brick.
	 * 
	 * @return
	 */
	public String getMACAddress() {
		return info.deviceAddress;
	}

	/**
	 * Close bluetooth connection.
	 */
	public void disconnect() {
		try {
			dis.close();
			dos.close();
			conn.close();
		} catch (Exception e) {
			System.out.println("Couldn't close " + getDeviceName() + " connection.");
			System.out.println(e.getMessage());
		}
	}

	public boolean sendCommand(char code, double param) {
		boolean success = true;
		try {
			dos.writeChar(code);
			dos.writeDouble(param);
			dos.flush();

		} catch (IOException ioe) {
			success = false;
			System.out.println("IO Exception writing bytes:");
			System.out.println(ioe.getMessage());
		}

		try {
			System.out.println("Received " + dis.readChar());
			System.out.println("Received " + dis.readDouble());
		} catch (IOException ioe) {
			success = false;
			System.out.println("IO Exception reading bytes:");
			System.out.println(ioe.getMessage());
		}
		return success;
	}
}
