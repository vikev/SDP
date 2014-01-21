
// Class to be used for calculating how much our robot can rotate in one frame
// Untested and will require package imports
// author s1143704

public class DistanceCalculator {
    private static final double TARGET_DIST = 10.0;
    
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
