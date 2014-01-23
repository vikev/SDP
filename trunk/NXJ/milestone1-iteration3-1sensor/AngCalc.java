
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require some package imports
// author s1143704

import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.nxt.Button;
import lejos.util.Delay;

public class AngCalc {
	
    public static double TARGET_ANG = 22.5;
    
    //todo: should solve for x + y * (frames-1) = angle
    //to accommodate for 1 (or more) frames of acceleration (x) followed by constant rate (y)
    //test results (as comments in LocationTracker.java/FRAME_ANGLE) suggests values of
    //x = -6.5, y = 0.445 fit for 22.5,45,90,135,180 deg rotation
    
    //also, testing this live fails, fuck it
    public static void main(String[] args){
        int frame = 0;
        int[] frames = new int[5];
        double q;
        DifferentialPilot pil = new DifferentialPilot(Travel.WHEEL_DIAM,Travel.TRACK_BASE,Motor.A,Motor.C,true);
        pil.setRotateSpeed(Travel.ROTATE_SPEED);
        for(int i=0;i<10;i++){
        	pil.rotate(TARGET_ANG,true);
        	frame = 0;
	        while(pil.isMoving()){
	            frame++;
	            Delay.msDelay(Travel.CLOCK_PERIOD);
	        }
	        System.out.println(frame);
	        Delay.msDelay(2000);
	        frames[i]=frame;
        }
        q = ((double)(frames[0] + frames[1] + frames[2] + frames[3] + frames[4]))/5.0;
        System.out.println(q);
        System.out.println(TARGET_ANG/q);
        Button.waitForAnyPress();
    }
}
