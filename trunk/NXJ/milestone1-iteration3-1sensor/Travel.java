
// Main class for the robot which will conduct milestone 1

import lejos.nxt.Motor;
import lejos.nxt.Button;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

public class Travel {
	// Using all CM now
    private static final double ROTATE = 90.0, TRAVEL_DIST = 10.0; //both are completely arbitrary
    private static final String FORWARD = "Forward Movement", RIGHT = "On White";
    
    public static final double WHEEL_DIAM = 5.715, TRACK_BASE = 15.875, ROTATE_SPEED = 90.0 / 2.0, TRAVEL_SPEED = 15.24;
    
	static DifferentialPilot pilot;
    static String state = "";
    
    private static void assertState(String n){
        if(! state.equals(n)){
            state = n;
            System.out.println(n);
        }
    }
	
	public static void main(String[] args){
        DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAM, TRACK_BASE,Motor.A,Motor.C, true);
        pilot.setRotateSpeed(ROTATE_SPEED);
        pilot.setTravelSpeed(TRAVEL_SPEED);
        RelativeLocation.reset();
        while(!Button.ENTER.isDown()){
            int q = Direct.getDirection();
            if(q==Direct.DIR_RIGHT){
                assertState(RIGHT);
                pilot.rotate(ROTATE,true); // The TRUE flag means return immediately, this should
                                           // stop the robot from doing that repetitive turn,stop
                                           // thing. Haven't tested because I'm at home.
                RelativeLocation.addAng();
            }else{
                assertState(FORWARD);
                RelativeLocation.addDist();
                pilot.travel(TRAVEL_DIST,true);
            }
		}
        
        Delay.msDelay(2000);
        System.out.println("Rotating to Return Ang");
        pilot.rotate(RelativeLocation.returnAng());
       
        Delay.msDelay(2000);
        System.out.println("Going to Return Dist");
        pilot.travel(RelativeLocation.returnDist());
        
        Delay.msDelay(2000);
        System.out.println("Resetting facing angle");
        pilot.rotate(RelativeLocation.facing*-1.0); //Initial angle is always zero :3
    }
}
