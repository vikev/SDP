import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class LSensor {
    static LightSensor sens = new LightSensor(SensorPort.S1);
			
    public static int getValue(){
        return sens.getLightValue();
    }
}