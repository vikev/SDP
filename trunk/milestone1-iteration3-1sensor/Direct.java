
public class Direct {
	public static int DIR_RIGHT = 0, DIR_STRAIGHT = 1;
	
    public static int getDirection(){
        int val = LSensor.getValue();
        if(val>=50){
            return DIR_RIGHT;
        }
        return DIR_STRAIGHT;
    }
}
