
public class Direct {
    private static final int THRESHOLD = 50
	public static int DIR_RIGHT = 0, DIR_STRAIGHT = 1;
	
    public static int getDirection(){
        int val = LSensor.getValue();
        if(val>=THRESHOLD){
            return DIR_RIGHT;
        }
        return DIR_STRAIGHT;
    }
}
