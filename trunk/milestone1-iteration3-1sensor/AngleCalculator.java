
// Class to be used for calculating how much our robot can rotate in one frame

public class AngleCalculator {
    private static final double TARGET_ANG = 90.0;
    private static double facing = 0.0;
    
    public static void main(String[] args){
        int frame = 0;
        Motor.rotateTo(TARGET_ANG);
        while(! Motor.isStopped()){
            frame++;
        }
        System.out.println(frame); // TARGET_ANG /  frame is the number of degrees rotated per frame
        Button.waitForPress();
    }
}
