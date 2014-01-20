
// Class to be used for calculating how much our robot can rotate in one frame

public class DistanceCalculator {
    private static final double TARGET_DIST = 10.0;
    private static double dist = 0.0;
    
    public static void main(String[] args){
        int frame = 0;
        Motor.travel(TARGET_DIST,true);
        while(! Motor.isStopped()){
            frame++;
        }
        System.out.println(frame); // TARGET_DIST /  frame is the distnace per frame
        Button.waitForPress();
    }
}
