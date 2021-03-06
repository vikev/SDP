// Needs proper files imported - NOT been debugged or tested
// author s1143704

import java.lang.Math.*;

import lejos.nxt.LCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

public class LocationTracker extends Thread {
	/**
	 * The angle we rotated in a single frame, in degrees
	 * 0.386 for 45deg
	 * 0.415 for 90deg
	 * 0.424 for 135deg
	 * 0.430 for 180deg
	 */
    private static final double FRAME_ANGLE = 0.435; //To be calculated using AngleCalculator
    private static final double FRAME_ANGLE_FST = -16.5;	//heavy maths suggest this should be -16.5, lmao
    /**
     * The distance we travel in a single frame, in centimeters
     */
    private static final double FRAME_DIST =  0.13673655; //To be calculated using DistanceCalculator
    
    
    private static final double DEGTORAD = Math.PI / 180.0;
    private static final double RADTODEG = 180.0 / Math.PI;
    
    private static final boolean debug = true;
    
    /**
     * The time taken to execute the body of the run() loop, in milliseconds
     * Might be completely unnecessary
     */
    private static final int codeSpeed = 0;
    
    public double x = 0.0, 
    		y = 0.0, 
    		facing = 0.0;
    
    public boolean running = true;
    
    /**
     * Normalizes the given angle, returning an angle in the range (-180;180]
     * @param angle the angle to normalize
     * @return the normalized angle, in the range (-180;180]
     */
    private static double normalizeAngle(double angle){
    	angle%=360;	// (-360;360]
    	angle+=360; // (0; 720]
    	angle%=360;	// (0; 360]
    	if (angle>180) 
    		angle-=360;	// (-180; 180]
    	return angle;
    	
//            while(ang>180.0){
//                    ang-=360.0;
//            }
//            while(ang<-180.0){
//                    ang+=360.0;
//            }
//            return ang;
    }
    
    
    private void addAng(double deg){
        facing+=deg;
    }
    
    private void addAng() { //this method adds the angle turned in one FRAME
    	if(rotating)
    		addAng(FRAME_ANGLE);
    	else
    		addAng(FRAME_ANGLE_FST);
    }
    
    private void subAng() {
    	if(rotating)
    		addAng(-FRAME_ANGLE);
    	else
    		addAng(-FRAME_ANGLE_FST);
    }
    
    private void addDist(double cm){ // Travel distance method will need to factor in gear ratio
        x += cm * Math.cos(facing*DEGTORAD);
        y += cm * Math.sin(facing*DEGTORAD);
    }
    
    private void addDist(){
        addDist(FRAME_DIST);
    }

	final static int lcdY = 7;
	
	private boolean rotating, moving;
	
    public void run() {
    	while(running) {
    		
    		if(debug) {
    	    	LCD.clear(lcdY);
    	    	LCD.drawInt((int)(x), 0, lcdY);
    	    	LCD.drawInt((int)(y), 5, lcdY);
    	    	LCD.drawInt((int)(facing), 10, lcdY);
    		}
    		
    		//are we rotating
    		if(M1.isRotating()) {
    			if(M1.isRotatingRight()) {
    				subAng();
    			}
    			else {
    				addAng();
    			}
    			
    		}
    		
    		//are we moving
    		moving = M1.isMovingForward();
    		if(moving) {
    			addDist();
    		}
    		
    		rotating = M1.isRotating();
    		
    		//wait prescribed time minus the time taken to execute this 
    		Delay.msDelay(M1.CLOCK_PERIOD_MS - codeSpeed);
    	}
    }
    
    public void printInfo() {
    }
    
    public void printReturnInfo() {
    	LCD.clear(1);
    	LCD.drawString((int)getReturnAngle() + "deg", 0, 1);
    	LCD.drawString((int)getReturnDistance() + "cm", 7, 1);
    }
    
    public double getReturnAngle() {
        return normalizeAngle(Math.atan2(y,x)*RADTODEG - 180.0 - facing);	//seems ok; not thoroughly tested
    }

    public double getReturnDistance() {
        return Math.sqrt(x * x + y * y);
    }
}
