
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
        DifferentialPilot pil = new DifferentialPilot(2.25f,6.25f,Motor.A,Motor.C,true);
        pil.setRotateSpeed(90.0/0.8);
        while(true){
        	
        	pil.rotate(TARGET_ANG,true);
        	frame = 0;
	        while(pil.isMoving()){
	            frame++;
	        }
	        System.out.println(frame);
	        Button.waitForAnyPress();
	        //105962
	        //123590
	        //141247
	        //158537
	        //88088
	        
	        //123484.8 frames / 90 degrees = 1372.05 frames per degree @ 90.0/0.8 degrees per second
        }
    }
}
