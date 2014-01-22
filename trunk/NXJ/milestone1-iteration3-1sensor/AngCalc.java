
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require some package imports
// author s1143704

import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.nxt.Button;

public class AngCalc {
    public static double TARGET_ANG = 90.0;
    
    public static void main(String[] args){
        int frame = 0;
        DifferentialPilot pil = new DifferentialPilot(2.25f,5f,Motor.A,Motor.C,true);
        while(true){
        	pil.rotate(TARGET_ANG,true);
	        while(pil.isMoving()){
	            frame++;
	        }
	        System.out.println(frame);
	        Button.waitForAnyPress();
        }
    }
}
