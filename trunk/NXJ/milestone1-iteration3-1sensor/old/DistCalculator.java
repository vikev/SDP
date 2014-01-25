
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require package imports
// author s1143704

import lejos.nxt.Motor;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

public class DistCalculator {
    
    private static final boolean goBack = true;
    
    private static int[] distances = new int[] { 2, 5, 10, 
    							15, 20, 30 };
    
    private static int doTest(int nTests, int distance) {
    	int nFrames = 0;
    	int totalFrames = 0;
        for(int i=0; i<5; i++) {
        	nFrames = 0;
        	
        	pilot.travel(distance,true);
	        while(pilot.isMoving()){
	            nFrames++;
	            Delay.msDelay(Travel.CLOCK_PERIOD);
	        }
	        totalFrames += nFrames;
	        if(goBack)
	        	pilot.travel(-distance);
	        Delay.msDelay(500);
        }
        int avgFrames = totalFrames / nTests;
        return avgFrames;
    }
    
    static DifferentialPilot pilot;
    
    static int getX(int distId) {
    	return (distId % 4) * 4;
    }
    
    static int getY(int distId) {
    	return (distId / 4) * 2;
    }
    
    static int getRepeats(int distId) {
    	return 5 * (3 - (distId / 3));
    }
    
    public static void main(String[] args){
    	pilot = new DifferentialPilot(Travel.WHEEL_DIAM,Travel.TRACK_BASE,Motor.A,Motor.C,true);
    	pilot.setTravelSpeed(Travel.TRAVEL_SPEED);

    	for(int i = 0; i < distances.length; i++) {
    		int x = getX(i);
    		int y = getY(i);
    		int reps = getRepeats(i);
    		
    		int ans = doTest(reps, distances[i]);
    		
    		LCD.drawInt(distances[i], x, y);    		
    		LCD.drawInt(ans, x, y);
    	}
    	
        Button.waitForAnyPress();
    }
}
