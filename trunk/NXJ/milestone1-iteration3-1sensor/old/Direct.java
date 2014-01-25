
// Class for calculating the new direction

public class Direct {
    private static final int THRESHOLD = 40;
	public static int DIR_RIGHT = 0, DIR_STRAIGHT = 1;
	
    public static int getDirection(){
        LSensor.getValue();
        if(LSensor.l>=THRESHOLD || LSensor.r>=THRESHOLD){
        	if (!LocationT.distReach)
            {
            	LocationT.distReach = true;
            	LocationT.dist = 0.0;
            }
        	return DIR_RIGHT;
        }
        return DIR_STRAIGHT;
    }
}
