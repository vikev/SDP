
public class AngleCalculator {
    private static final int TARGET_ANG = 90.0;
    private static double facing = 0.0;
    
    public static void main(String[] args){
        int frame = 0;
        Motor.rotateTo(TARGET_ANG)
        while(! Motor.isStopped()){
            frame++;
        }
        System.out.println(frame);
        Button.waitForPress();
    }
}
