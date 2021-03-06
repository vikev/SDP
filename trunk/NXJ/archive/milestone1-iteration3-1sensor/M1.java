
// Main class for the robot which will conduct milestone 1

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.Button;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

public class M1 { // All values in centimeters
	
	// Both values completely arbitrary (independent of performance)
    private static final double ROTATE = 90.0, TRAVEL_DIST = 10.0;
    
    private static final String STATE_FORWARD = "Forward Movement", 
    		STATE_RIGHT = "On White";
    
    public static final int FPS = 100, CLOCK_PERIOD_MS = 1000 / FPS;
    public static final boolean DEBUG = false;
    public static final double WHEEL_DIAM = 5.715, TRACK_BASE = 15.875, 
    	ROTATE_SPEED = 90.0 / 4.8, TRAVEL_SPEED = 12.24;
    
	static DifferentialPilot pilot;
    static String state = "";
    static LocationTracker tracker; // Only used in debug mode
    static double minDist = 0.0;
    static boolean started = false;

    /**
     * Returns whether the robot is currently moving
     */
    static boolean isMovingForward() {
    	return state.equals(STATE_FORWARD);
    }
    
    /**
     * Returns whether the robot is currently rotating
     */
    static boolean isRotating() {
    	return state.equals(STATE_RIGHT);
    }
    
    /**
     * Returns whether the robot is rotating right.
     */
    static boolean isRotatingRight() {
    	return state.equals(STATE_RIGHT);
    }
    
    /**
     * Sets the state of the robot
     * @param n the new state
     */
    private static void setState(String n){
        if(!state.equals(n)){
            state = n;
            LCD.clear(0);
            LCD.drawString(n, 0, 0);
        }
    }
    
	public static void main(String[] args){
		// Get tracking location
		System.out.println("ENTER for attacker, BACK for defender");
		int btn = Button.waitForAnyPress();
		if(btn==Button.ID_ENTER){
			minDist = 170.0063+30.0;
		}else{
			minDist = 151.8337+25.0;
		}
		
		LCD.clear();
		System.out.println("Beginning..");
		
		// Initialise the pilot
        DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAM, TRACK_BASE, 
        	Motor.A, Motor.C, true);
        pilot.setRotateSpeed(ROTATE_SPEED);
        pilot.setTravelSpeed(TRAVEL_SPEED);
        
        // Initialise the tracker; disabled since we're not calibrating
        if(DEBUG){
	        tracker = new LocationTracker();
	        // No need to terminate the thread explicitly this way
	        tracker.setDaemon(true); 
	        tracker.start();
        }
        
        // Wait a moment to prevent a button read in rapid succession
        Delay.msDelay(500);
        
    	// Loop already has an exit condition, so we can just:
    	while(DistanceTracker.dist<minDist){
            int newDirection = Direct.getDirection();
            if(newDirection == Direct.DIR_RIGHT) {
            	if (! started){
            		started = true;
            		DistanceTracker.reset();
            	}
                setState(STATE_RIGHT);
                pilot.rotateRight();
            } else {
                setState(STATE_FORWARD);
                pilot.travel(TRAVEL_DIST,true);
                DistanceTracker.addDist();
            }
            Delay.msDelay(CLOCK_PERIOD_MS);
        }

        // Stop the robot instantly and don't terminate the program
        pilot.stop();
        LCD.clear();
        System.out.println("Target Reached");
        Button.waitForAnyPress();
    }
}
