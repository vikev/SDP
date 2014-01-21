
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require some package imports
// author s1143704

import lejos.nxt.Motor;
import lejos.nxt.Button;

public class AngleCalculator {
    public static int TARGET_ANG = 90;
    
    public static void main(String[] args){
        int frame = 0;
        Motor.A.rotateTo(TARGET_ANG, true);
        Motor.C.rotateTo(-TARGET_ANG,true);
        while(Motor.A.isMoving() || Motor.C.isMoving()){
            frame++;
        }
        System.out.println(frame);
        System.out.println((TARGET_ANG / frame));
        Button.waitForAnyPress();
    }
}
