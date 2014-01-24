
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
    
    /**
     * The frames per second that determine the clock period
     */
    public static final int FPS = 100;
    
    /**
     * The clock period in milliseconds
     */
    public static final int CLOCK_PERIOD = 1000 / FPS;
    
    private static final String STATE_FORWARD = "Move Forward", 
    		STATE_RIGHT = "Turn Right",
    		STATE_STOP = "Idle";
    
    public static final double WHEEL_DIAM = 0.41, 
    		TRACK_BASE = 1, 
    		ROTATE_SPEED = 90.0 / 2.0, 
    		TRAVEL_SPEED = 15.24;
    
	static DifferentialPilot pilot;
    static String state = STATE_STOP;

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
            int newDirection = Direct.getDirection();
            if(newDirection == Direct.DIR_RIGHT) {
                setState(STATE_RIGHT);
                pilot.rotate(ROTATE,true); //true flag works as expected

            } else {
                setState(STATE_FORWARD);
                pilot.travel(TRAVEL_DIST,true);
            }
            n++;
            Delay.msDelay(CLOCK_PERIOD);
		}
        System.out.println(n);
        
        //issue stop
        state = STATE_STOP;
        pilot.stop();
        
        double returnAng = tracker.getReturnAngle();
        double returnDist = tracker.getReturnDistance();
        
        tracker.printReturnInfo();
        System.out.println("Beginning Return..");
        
        Delay.msDelay(2000);
        System.out.println(returnAng);
        pilot.rotate(returnAng);
       
        Delay.msDelay(2000);
        System.out.println(returnDist);
        pilot.travel(returnDist);

        Delay.msDelay(2000);
        double rotateAng = -tracker.facing;
        System.out.println(rotateAng);
        pilot.rotate(rotateAng);
        
        Button.waitForAnyPress();
    }
}
