
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.robotics.navigation.DifferentialPilot;


public class Travel {
    private static final double ROTATE = -12.0, TRAVEL_SPEED = 10.0;
	static DifferentialPilot pilot;
	
	public static void go(){
		while(true){
            int q = Direct.getDirection();
            if(q==Direct.DIR_RIGHT){
                System.out.println("On white");
                pilot.rotate(ROTATE);
            }else{
                pilot.setTravelSpeed(TRAVEL_SPEED);  
                System.out.println("Forward movement");
                pilot.forward();
            }
		}
    }
	
	public static void main(String[] args){
        pilot = new DifferentialPilot(2.25f, 5.5f,(NXTRegulatedMotor) Motor.A,(NXTRegulatedMotor) Motor.C, true);
        go();
    }
}
