
// Main class for the robot which will conduct milestone 1

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.Button;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.robotics.navigation.DifferentialPilot;

public class Travel {
    private static final double ROTATE = -12.0, TRAVEL_SPEED = 10.0; //both are completely arbitrary
    private static final String FORWARD = "Forward Movement", RIGHT = "On White";
    
	static DifferentialPilot pilot;
    static String state = ""
    
    private static void assertState(String n){
        if(! state.equals(n)){
            state = n;
            System.out.println(n);
        }
    }
	
	public static void main(String[] args){
        pilot = new DifferentialPilot(2.25f, 5.5f,(NXTRegulatedMotor) Motor.A,(NXTRegulatedMotor) Motor.C, true);
        
        while(! button.isPressed()){
            int q = Direct.getDirection();
            if(q==Direct.DIR_RIGHT){
                assertState(RIGHT);
                RelativeLocation.subAng();
                pilot.rotate(ROTATE,true); // The TRUE flag means return immediately, this should
                                           // stop the robot from doing that repetitive turn,stop
                                           // thing. Haven't tested because I'm at home.
            }else{
                pilot.setTravelSpeed(TRAVEL_SPEED); 
                assertState(FORWARD);
                RelativeLocation.addDist();
                pilot.travel(TRAVEL_SPEED,true);
            }
		}
        
        pilot.rotateTo(RelativeLocation.returnAng());
        pilot.travel(RelativeLocation.returnDist());
        pilot.rotateTo(0.0); //Initial angle is always zero :3
    }
}
