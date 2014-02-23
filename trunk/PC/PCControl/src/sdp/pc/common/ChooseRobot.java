package sdp.pc.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static sdp.pc.common.Constants.*;

/**
 * ChooseRobot is a static class for selecting a robot. When initialising a
 * TCPClient, use a choose robot method as the parameter.
 */
public class ChooseRobot {

	/**
	 * Message to display to users when they choose dialog()
	 */
	private static final String QUERY = "Select NXT brick to connect. 'A' for "
			+ "attacker (SDP 9A), 'D' for defender (SDP 9B)";

	/**
	 * Message to display to users when they fail to type a real robot code.
	 */
	private static final String ERROR = "Unrecognized code. Use 'A' or 'D'.";

	/**
	 * Show a dialog in the console for the user to select an NXT brick from a
	 * character value.
	 * 
	 * @return a constant int which represents a robot.
	 */
	public static int dialog() {

		// Initialise required variables
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String choice = "";

		// Display the command to choose a robot
		System.out.println(QUERY);

		// Read input until we have an answer
		while (true) {
			try {
				choice = br.readLine();
				if (choice.equalsIgnoreCase("a")) {
					return ATTACKER;
				}

				if (choice.equalsIgnoreCase("d")) {
					return DEFENDER;
				}

				// Unrecognised code, err
				System.out.println(ERROR);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * static method for getting the attacker
	 * 
	 * @return Constants.ATTACKER
	 */
	public static int attacker() {
		return ATTACKER;
	}

	/**
	 * static method for getting the defender
	 * 
	 * @return Constants.DEFENDER
	 */
	public static int defender() {
		return DEFENDER;
	}
}
