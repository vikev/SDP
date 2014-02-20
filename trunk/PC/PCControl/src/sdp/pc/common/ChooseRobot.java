package sdp.pc.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * TODO: What is this class and why do we have it?
 *
 */
public class ChooseRobot {
	public static int dialog() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int robot = 0;
		String choice = "";
		System.out
				.println("Select robot to connect to ('A' - attacker(SDP 9A); 'D' - defender(SDP 9B)): ");

		while (true) {
			try {
				choice = br.readLine();
				if (choice.equalsIgnoreCase("a")) {
					robot = Constants.ATTACKER;
					break;
				}

				if (choice.equalsIgnoreCase("d")) {
					robot = Constants.DEFENDER;
					break;
				}
				System.out
						.println("Wrong code enter. Please choose from 'A' and 'D': ");
			} catch (IOException e) {
				e.printStackTrace();
				System.out
						.println("Error occured while reading the input. Please try again...");
			}

		}

		return robot;
	}
}
