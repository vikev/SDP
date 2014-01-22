package sdp.nxj.bluetooth;
import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;

public class DiffPilot {
	private DifferentialPilot pilot;
	public DiffPilot(){
		pilot = new DifferentialPilot(5.5, 12.5, Motor.A, Motor.C, true);
	}
	
	public void driveForward(double distance){
		if (distance == 0) {       // interpret a distance of zero to mean
			pilot.forward();       // drive forward forever
		} else {
			pilot.travel(distance);
		}
	}
	
	public void driveBackward(double distance){
		if (distance == 0) {        // interpret a distance of zero to mean
			pilot.backward();       // drive backward forever
		} else {
			pilot.travel(-distance);
		}
	}
	
	public void turnLeft(double distance){
		pilot.rotate(distance);
	}
	
	public void turnRight(double distance){
		pilot.rotate(-distance);
	}
	
	public void stopNow(){
		pilot.stop();
	}
}
