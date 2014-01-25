
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require some package imports
// author s1143704

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.nxt.Button;
import lejos.util.Delay;

public class AngCalc {
	
    public static final double TARGET_ANG = 5;
    
    
    private static final int[] angles = new int[] { 5, 10, 15, 20,
    												30, 40, 45, 70,
    												90, 135, 180, 225 };
    
    private static int getRepeats(int angleId) {
    	return 5 * (3 - angleId / 4);	//5 tests for the first 4 vals, 10 for next, 15 for last
    }
    
    private static int getX(int angleId) {
    	return (angleId % 4) * 4;
    }
    
    private static int getY(int angleId) {
    	return (angleId / 4) * 2;
    }
    
    private static int doTest(int repeats, double angle, int x, int y) {
        int nFrames = 0;
        int totalFrames = 0;
        
        for(int i=0; i < repeats; i++) {
        	
        	pilot.rotate(angle,true);
        	nFrames = 0;
        	
	        while(pilot.isMoving()){
	            nFrames++;
	            Delay.msDelay(Travel.CLOCK_PERIOD);
	        }

	        totalFrames += nFrames;
	        Delay.msDelay(500);
        }
        int avgFrames = totalFrames / repeats;
        LCD.drawInt(avgFrames, x, y);
        return avgFrames;
    }
    static DifferentialPilot pilot;
    
    //todo: should solve for x + y * (frames-1) = angle
    //to accommodate for 1 (or more) frames of acceleration (x) followed by constant rate (y)
    //test results (as comments in LocationTracker.java/FRAME_ANGLE) suggests values of
    //x = -16.5, y = 0.445 fit for 22.5,45,90,135,180 deg rotation
    
    //also, testing this live fails, fuck it
    public static void main(String[] args) {
        //setup pilot
        pilot = new DifferentialPilot(Travel.WHEEL_DIAM, Travel.TRACK_BASE, Motor.A, Motor.C, true);
        pilot.setRotateSpeed(Travel.ROTATE_SPEED);
        
        for(int i = 0; i < angles.length; i++) {
        	int x = getX(i);
        	int y = getY(i);
        	int rep = getRepeats(i);
        	int ans = doTest(rep, angles[i], x, y);
        	LCD.drawInt(angles[i], x, y);
        	LCD.drawInt(ans, x, y + 1);
        }
        Button.waitForAnyPress();

    }
    
    
}
