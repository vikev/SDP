package sdp.pc.robot.arbitrators;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JWindow;

import sdp.pc.common.ChooseRobot;
import sdp.pc.robot.pilot.Robot;
import sdp.pc.vision.Vision;
import sdp.pc.vision.WorldState;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;

public class ArbiterController extends JFrame {
	
	WorldState state = new WorldState();
	
	Robot attacker, defender;
	
	Arbiter attackerArbiter, defenderArbiter;

	private boolean connected;
	private JButton btnConnect;

	public static void main(String[] args) {
		(new ArbiterController()).setVisible(true);
	}
	
	public ArbiterController() {
		setSize(new Dimension(200, 100));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				disconnect();
			}
		});
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				connect();
			}
		});
		getContentPane().add(btnConnect, BorderLayout.NORTH);
		
		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				start();
			}
		});
		getContentPane().add(btnStart, BorderLayout.SOUTH);
		
		
		Vision.InvokeNew(state);
//		JOptionPane.showMessageDialog(null,"Please continue when the vision is calibrated and running!",
//				"Master Arbiter", JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	protected void start() {
		// TODO Auto-generated method stub
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				attackerArbiter.start();
				//defenderArbiter.start();
			}
			
		});
		t.setDaemon(true);
		t.start();
	}


	protected void disconnect() {
		if(!connected)
			return;
		
		System.out.print("Disconnecting... ");
		try {
			attacker.stop();
			attacker.closeConnection();
			defender.stop();
			defender.closeConnection();
			
			System.out.println("done!");
			connected = false;
		}
		catch(Exception e) {
			System.out.println("fail!");
		}
	}

	protected void connect() {
		if(connected)
			return;

		System.out.print("Connecting... ");
		try {
			int myTeam = state.getOurColor();
			int attackerId = state.getDirection();
			int defenderId = 1 - state.getDirection();
			
	
			// Connect to robots
			//defender = new Robot(ChooseRobot.defender(), state, myTeam, defenderId);
			attacker = new Robot(ChooseRobot.attacker(), state, myTeam, attackerId);
	
			attackerArbiter = new AttackerArbiter(attacker);
			defenderArbiter = new DefenderArbiter(defender);
			
			connected = true;
			System.out.println("done!");
		}
		catch(Exception e) {
			System.out.println("fail!");
		}
	}
	
	protected void onBtnConnectClicked() {
		if(!connected) {
			connect();
			btnConnect.setText("Disconnect");
		}
		else {
			disconnect();
			btnConnect.setText("Connect");
		}
	}


}
