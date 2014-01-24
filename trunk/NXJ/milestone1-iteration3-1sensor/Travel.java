
// Main class for the robot which will conduct milestone 1

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.Button;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

public class Travel {
	// Using all CM now
    private static final double ROTATE = 90.0, TRAVEL_DIST = 10.0; //both are completely arbitrary
    
    public static final int FPS = 100;
    
    /**
     * The clock period in milliseconds
     */
    public static final int CLOCK_PERIOD = 1000 / FPS;
    
    private static final String STATE_FORWARD = "Forward Movement", 
    		STATE_RIGHT = "On White";
    
    public static final double WHEEL_DIAM = 5.715, TRACK_BASE = 15.875, ROTATE_SPEED = 90.0 / 3.8, TRAVEL_SPEED = 12.24;
    
	static DifferentialPilot pilot;
    static String state = "";

    /**
     * Returns whether the robot is currently moving
     */
    static boolean isMoving() {
    	return state.equals(STATE_FORWARD);
    }
    
    /**
     * Returns whether the robot is currently rotating
     */
    static boolean isRotating() {
    	return state.equals(STATE_RIGHT);	// || state == STATE_RIGHT, for ex.
    }
    
    /**
     * If the robot is rotating, returns whether it rotates left (true) or right (false)
     */
    static boolean isRotatingLeft() {
    	return !state.equals(STATE_RIGHT);	//it's left when it's not right :D
    }
    
    /**
     * Sets the state of the robot
     * @param n the new state
     */
    private static void setState(String n){	//setter, not assertion
        if(!state.equals(n)){
            state = n;
            LCD.clear(0);
            LCD.drawString(n, 0, 0);
        }
    }
	
    static LocationTracker tracker;
    
	public static void main(String[] args){
		
		//Initialise the pilot
        DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAM, TRACK_BASE,Motor.A,Motor.C, true);
        pilot.setRotateSpeed(ROTATE_SPEED);
        pilot.setTravelSpeed(TRAVEL_SPEED);
        
        //Initialise the tracker
        tracker = new LocationTracker();
        tracker.setDaemon(true);	//no need to terminate the thread explicitly this way
        tracker.start();
        
        
        double d = 0.0;
        long n = 0;
        
        while(!Button.ENTER.isDown()) {
        	// attacker good angle  175.8432032
        	// attacker bad angle  167.7757468   172.9999999 
        	// goalkeeper good angle 158.484817
        	// goalkeeper bad angle 152.051043  153.965355
        	if (LocationT.dist >= 170.0063 && LocationT.dist <= 172.0063) break; // attacker stopping condition 
        	//if (LocationT.dist >= 151.8337 && LocationT.dist <= 153.6337) break; // goalkeeper stopping condition
            int newDirection = Direct.getDirection();
            if(newDirection == Direct.DIR_RIGHT) {
                setState(STATE_RIGHT);
                pilot.setRotateSpeed(ROTATE_SPEED);
                pilot.rotateLeft(); //true flag works as expected

            } else {
                setState(STATE_FORWARD);
                pilot.travel(TRAVEL_DIST,true);
                LocationT.addDist(LocationT.FRAME_DIST);
            }
            n++;
            Delay.msDelay(CLOCK_PERIOD);
            
        }
        System.out.println(n);
        System.out.println("Dist frames for attacker: " + LocationT.dist);
        
        /*
        //issue stop
        state = "stop";	//todo: remove
        pilot.stop();
        
        
        tracker.printReturnInfo();
        System.out.println("Beginning Return..");
        
        Delay.msDelay(2000);
        d = tracker.getReturnAngle();
        System.out.println(d);
        pilot.rotate(d);
       
        Delay.msDelay(2000);
        d = tracker.getReturnDistance();
        System.out.println(d);
        pilot.travel(d);
         
        Delay.msDelay(2000);
        d = -tracker.facing;
        System.out.println(d);
        pilot.rotate(d);
        */
        Button.waitForAnyPress();
        Button.waitForAnyPress();
    }
}
