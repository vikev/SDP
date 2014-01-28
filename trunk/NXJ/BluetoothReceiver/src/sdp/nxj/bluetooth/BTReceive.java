package sdp.nxj.bluetooth;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

public class BTReceive {

	public static void main(String [] args)  throws Exception 
	{
		DiffPilot pilot = new DiffPilot();
		
		String connected = "Connected";
        String waiting = "Waiting...";
        String closing = "Closing...";
        
		while (true)
		{
			LCD.drawString(waiting,0,0);
			LCD.refresh();

	        BTConnection btc = Bluetooth.waitForConnection();
	        
			LCD.clear();
			LCD.drawString(connected,0,0);
			LCD.refresh();	

			DataInputStream dis = btc.openDataInputStream();
			DataOutputStream dos = btc.openDataOutputStream();
			
			while(true) {
				try{
				char c = dis.readChar();
				double distance = dis.readDouble();
				dos.writeChar(c);
				dos.writeDouble(distance);
				dos.flush();
				
				switch(c){
				case 'f':
					pilot.driveForward(distance);
					break;
				case 'b':
					pilot.driveBackward(distance);
					break;
				case 'l':
					pilot.turnLeft(distance);  // distance is angle to turn in degrees
					break;
				case 'r':
					pilot.turnRight(distance); 
					break;
				case 'k':
					Kicker.kick(distance);
				case 's':
					pilot.stopNow();   // stop robot immediately
					break;
				}
				}catch(Exception e){
					System.out.println(e.getMessage());
					break;
				}
				
			}
			
			dis.close();
			dos.close();
			Thread.sleep(100); // wait for data to drain
//			LCD.clear();
			LCD.drawString(closing,0,0);
			LCD.refresh();
			btc.close();
  			LCD.clear();
		}
	}
}

