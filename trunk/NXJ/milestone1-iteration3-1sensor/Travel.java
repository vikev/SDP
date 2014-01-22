
// Main class for the robot which will conduct milestone 1

import lejos.nxt.Motor;
import lejos.nxt.Button;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

public class Travel {
	// Using all CM now
    private static final double ROTATE = 90.0, TRAVEL_DIST = 10.0; //both are completely arbitrary
    public static final double CLOCK_PERIOD = 1.0/100.0;
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
        double d = 0.0;
        long n = 0;
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
            n++;
            Delay.msDelay((int)(1000.0*CLOCK_PERIOD));
		}
        System.out.println(n);
        pilot.stop();
        System.out.println("Beginning Return..");
        
        Delay.msDelay(2000);
        d = RelativeLocation.returnAng();
        System.out.println(d);
        pilot.rotate(d);
       
        Delay.msDelay(2000);
        d = RelativeLocation.returnDist();
        System.out.println(d);
        pilot.travel(d);
        
        Delay.msDelay(2000);
        d = RelativeLocation.facing*-1.0;
        System.out.println(d);
        pilot.rotate(d);
        
        Button.waitForAnyPress();
    }
}
