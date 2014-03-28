package sdp.pc.relay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;
import lejos.pc.comm.NXTInfo;

/**
 * A bluetooth connection between a PC and a robot
 */
public class BTConnection {

	/**
	 * Recognises if a bluetooth connection has been made or not
	 */
	private boolean isConnected = false;

	/**
	 * The NXT Connector (leJOS class)
	 */
	private NXTConnector conn;

	/**
	 * Connection mode
	 */
	private int mode;

	/**
	 * The NXTInfo (leJOS class)
	 */
	private NXTInfo info;

	/**
	 * The Java Data Output Stream
	 */
	private DataOutputStream dos;

	/**
	 * The Java Data Input Stream
	 */
	private DataInputStream dis;

	/**
	 * Constructor for building a connection with an NXT brick
	 * 
	 * @param info
	 *            NXT connection details
	 * @param mode
	 *            Connection NXTComm constant TODO: No, it's an integer?
	 */
	public BTConnection(NXTInfo info, int mode) {

		// Build the bluetooth connection
		this.info = info;
		this.mode = mode;
		conn = new NXTConnector();
		conn.addLogListener(new NXTCommLogListener() {

			/**
			 * TODO: What does this do? I've never seen logging of messages
			 */
			public void logEvent(String message) {
				System.out.println("BTSend Log.listener: " + message);
			}

			/**
			 * Builds a log event for throwables, have no idea if this works or
			 * not.
			 */
			public void logEvent(Throwable throwable) {
				System.out.println("BTSend Log.listener - stack trace: ");
				throwable.printStackTrace();
			}
		});
	}

	public boolean connect() {
		if (conn.connectTo(info, mode)) {

			// TODO: Is it valid to assert the connection completed here?
			isConnected = true;
			dos = new DataOutputStream(conn.getOutputStream());
			dis = new DataInputStream(conn.getInputStream());
			return true;
		} else {
			isConnected = false;
			return false;
		}
	}

	/**
	 * Get the name of the brick.
	 * 
	 * @return the name of the connected brick as a String
	 */
	public String getDeviceName() {
		return info.name;
	}

	/**
	 * Returns connection status. TODO: If the robot gets disconnected, does
	 * this still return true? If so, what's the point?
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * Get the address of the brick.
	 * 
	 * @return the MAC address of the connected brick as a String
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
			isConnected = false;
		} catch (Exception e) {
			System.out.println("Couldn't close " + getDeviceName()
					+ " connection.");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Sends a byte array to the robot which corresponds to a command
	 * 
	 * @param data
	 *            data[0] - command, data[1]+data[2] - short power value
	 * @return
	 */
	public boolean sendCommand(byte[] data) {
		boolean success = true;
		try {
			dos.write(data);
			dos.flush();

		} catch (IOException ioe) {
			success = false;
			System.out.println("IO Exception writing bytes:");
			System.out.println(ioe.getMessage());
		}
		return success;
	}

}
