package sdp.pc.relay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import sdp.pc.common.Constants;
import sdp.pc.vision.settings.RadioActionListener;

public class CommandRelay {
	/**
	 * The device name by which to refer to brick A
	 */
	private static final String A_NAME = "SDP 9A";

	/**
	 * The device MAC address by which to refer to brick A
	 */
	private static final String A_MAC = "0016530BB5A3";

	/**
	 * The device name by which to refer to brick B
	 */
	private static final String B_NAME = "SDP 9B";

	/**
	 * The device MAC address by which to refer to brick B
	 */
	private static final String B_MAC = "001653077531";

	private NXTInfo brickA = new NXTInfo(NXTCommFactory.BLUETOOTH, A_NAME,
			A_MAC);

	private NXTInfo brickB = new NXTInfo(NXTCommFactory.BLUETOOTH, B_NAME,
			B_MAC);

	private BTConnection connBrickA = new BTConnection(brickA, NXTComm.PACKET);
	private BTConnection connBrickB = new BTConnection(brickB, NXTComm.PACKET);
	private BTConnection connToAttacker = connBrickA;
	private BTConnection connToDefender = connBrickB;

	private JFrame frame;
	private JRadioButton btnAttackerA;
	private JRadioButton btnAttackerB;
	private JButton btnConnectA;
	private JButton btnConnectB;

	private JButton actionA;
	private JButton actionB;
	private ConnectionThread serverAttacker = new ConnectionThread(
			connToAttacker, Constants.ATTACKER);

	private ConnectionThread serverDefender = new ConnectionThread(
			connToDefender, Constants.DEFENDER);

	private RadioActionListener attackerBrickListener = new RadioActionListener() {
		@Override
		public void selectedChanged(JRadioButton selectedButton,
				String actionCommand, boolean forced) {
			if (selectedButton == btnAttackerA) {
				connToAttacker = connBrickA;
				connToDefender = connBrickB;
				btnAttackerB.setSelected(false);
			} else {
				connToAttacker = connBrickB;
				connToDefender = connBrickA;
				btnAttackerA.setSelected(false);
			}
			updateRoles();
		}

	};

	private void updateRoles() {
		serverAttacker.changeConnection(connToAttacker);
		serverDefender.changeConnection(connToDefender);
	}

	private ActionListener actionListenerConnectA = new ActionListener() {
		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e) {
			if (connBrickA.connect()) {
				actionA.setLabel("Disconnect A");
				actionA.removeActionListener(actionListenerConnectA);
				actionA.addActionListener(actionListenerDisconnectA);
				updateRoles();
			}
		}

	};

	private ActionListener actionListenerConnectB = new ActionListener() {
		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e) {
			if (connBrickB.connect()) {
				actionB.setLabel("Disconnect B");
				actionB.removeActionListener(actionListenerConnectB);
				actionB.addActionListener(actionListenerDisconnectB);
				updateRoles();
			}
		}

	};

	private ActionListener actionListenerDisconnectA = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			connBrickA.disconnect();
			actionA.setLabel("Connect to A");
			actionA.addActionListener(actionListenerConnectA);
			actionA.removeActionListener(actionListenerDisconnectA);
		}
	};

	private ActionListener actionListenerDisconnectB = new ActionListener() {
		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e) {
			connBrickB.disconnect();
			actionB.setLabel("Connect to B");
			actionB.addActionListener(actionListenerConnectB);
			actionB.removeActionListener(actionListenerDisconnectB);
		}
	};

	private void run() {
		frame = new JFrame("Connection manager");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel settings = new JPanel();

		btnAttackerA = new JRadioButton();
		btnAttackerA.setName("Brick A");
		btnAttackerA.addActionListener(attackerBrickListener);
		btnAttackerA.setSelected(true);

		btnAttackerB = new JRadioButton();
		btnAttackerB.setName("Brick B");
		btnAttackerB.addActionListener(attackerBrickListener);

		settings.add(new JLabel("Select attacker:"));
		settings.add(new JLabel("Brick A"));
		settings.add(btnAttackerA);
		settings.add(new JLabel("Brick B"));
		settings.add(btnAttackerB);

		btnConnectA = new JButton("Connect to A");
		btnConnectA.addActionListener(actionListenerConnectA);
		btnConnectA.setSize(100, 50);

		actionA = btnConnectA;
		settings.add(actionA);

		btnConnectB = new JButton("Connect to B");
		btnConnectB.addActionListener(actionListenerConnectB);
		btnConnectB.setSize(100, 50);

		actionB = btnConnectB;
		settings.add(actionB);

		frame.add(settings);
		frame.setSize(400, 100);
		frame.setVisible(true);

		new Thread(serverAttacker).start();
		new Thread(serverDefender).start();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CommandRelay().run();
	}
}
