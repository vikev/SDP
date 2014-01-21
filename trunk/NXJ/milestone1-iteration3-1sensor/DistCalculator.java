
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require package imports
// author s1143704

import lejos.nxt.Motor;
import lejos.nxt.Button;

public class DistCalculator {
    private static int TARGET_DIST = 10;
    
    public static void main(String[] args){
        int frame = 0;
        Motor.A.rotateTo(TARGET_DIST,true);
        Motor.C.rotateTo(TARGET_DIST,true);
        while(Motor.A.isMoving() || Motor.C.isMoving()){
            frame++;
        }
        System.out.println(frame); // TARGET_DIST /  frame is the distnace per frame
        Button.waitForAnyPress();
    }
}
