
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require package imports
// author s1143704

import lejos.nxt.Motor;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

public class DistCalculator {
    private static int TRAVEL_DIST = 30; // cm
    
    public static void main(String[] args){
    	DifferentialPilot pil = new DifferentialPilot(Travel.WHEEL_DIAM,Travel.TRACK_BASE,Motor.A,Motor.C,true);
    	pil.setTravelSpeed(Travel.TRAVEL_SPEED);
        int frame = 0;
        double q;
        int[] frames = new int[5];
        for(int i=0;i<5;i++){
        	frame = 0;
        	pil.travel(TRAVEL_DIST,true);
	        while(pil.isMoving()){
	            frame++;
	        }
	        System.out.println(frame);
	        frames[i] = frame;
	        Delay.msDelay(2000);
        }
        q = ((double)(frames[0] + frames[1] + frames[2] + frames[3] + frames[4]))/5.0;
        System.out.println(q);
        System.out.println(TRAVEL_DIST/q);
        Button.waitForAnyPress();
    }
}
