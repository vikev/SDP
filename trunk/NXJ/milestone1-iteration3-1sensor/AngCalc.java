
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require some package imports
// author s1143704

import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.nxt.Button;
import lejos.util.Delay;

public class AngCalc {
    public static double TARGET_ANG = 90.0;
    
    public static void main(String[] args){
        int frame = 0;
        int[] frames = new int[5];
        double q;
        DifferentialPilot pil = new DifferentialPilot(Travel.WHEEL_DIAM,Travel.TRACK_BASE,Motor.A,Motor.C,true);
        pil.setRotateSpeed(Travel.ROTATE_SPEED);
        for(int i=0;i<5;i++){
        	pil.rotate(TARGET_ANG,true);
        	frame = 0;
	        while(pil.isMoving()){
	            frame++;
	            Delay.msDelay((int)(1000.0*Travel.CLOCK_PERIOD));
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
