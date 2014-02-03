package sdp.nxj.bluetooth;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.addon.RCXMotor;

public class PneumaticKicker extends Thread {
	
	private static RCXMotor kickerValve = new RCXMotor(MotorPort.B);
	
	public void run(){
		kickerValve.setPower(100);
		kickerValve.backward();
		try{ Thread.sleep(150); }
		catch(Exception e){}
		kickerValve.stop();
		try{ Thread.sleep(250); }
		catch(Exception e){}
		kickerValve.forward();
		try{ Thread.sleep(150); }
		catch(Exception e){}
		kickerValve.stop();
	}
}
