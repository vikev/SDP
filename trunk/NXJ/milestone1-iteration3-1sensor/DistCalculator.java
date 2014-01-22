
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require package imports
// author s1143704

import lejos.nxt.Motor;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.robotics.navigation.DifferentialPilot;

public class DistCalculator {
    private static int TRAVEL_DIST = 10; // Arbitrary
    
    public static void main(String[] args){
    	DifferentialPilot pil = new DifferentialPilot(2.25f,5f,Motor.A,Motor.C,true);
    	pil.travel(TRAVEL_DIST,true);
        int frame = 0;
        System.out.println("hello");
        while(true){
	        while(pil.isMoving()){
	            frame++;
	        }
	        System.out.println("frames: "+frame); // TARGET_DIST /  frame is the distnace per frame
	        Button.waitForAnyPress();
	        // 19197
	        // 18967
	        // 18686
	        // 18423
        }
    }
}
