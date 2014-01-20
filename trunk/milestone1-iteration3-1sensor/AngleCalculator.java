
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require some package imports
// author s1143704

public class AngleCalculator {
    private static final double TARGET_ANG = 90.0;
    
    public static void main(String[] args){
        int frame = 0;
        Motor.rotateTo(TARGET_ANG,true);
        while(! Motor.isStopped()){
            frame++;
        }
        System.out.println(frame);
        System.out.println(((int) TARGET_ANG / frame));
        Button.waitForPress();
    }
}
