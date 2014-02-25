
public enum Direction {
	LEFT (0x1),
	RIGHT (0x2),
	FORWARD (0x3),
	BACKWARD (0x4);
	
	int direction;
	
	public int getDirection(){
		return direction;
	}
	Direction(int n){
		direction = n;
	}

}
