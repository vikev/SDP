import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class ColorScanner {

	static LightSensor left = new LightSensor(SensorPort.S4);
	static LightSensor right = new LightSensor(SensorPort.S1);

	public static boolean isOnWhite() {
		return left.getLightValue() > 50 && right.getLightValue() > 50;
	}
	
	public static Direction getNewDirection(){
		if(left.getLightValue()>50&&right.getLightValue()<50){
			return Direction.RIGHT;
		}
		if(left.getLightValue()<50&&right.getLightValue()>50){
			return Direction.LEFT;
		}
		if(left.getLightValue()<50&&right.getLightValue()<50){
			return Direction.FORWARD;
		}
		return Direction.BACKWARD;
	}

}
