
// Class for getting light sensor value in an abstract manner

import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class LSensor {
    static LightSensor sens = new LightSensor(SensorPort.S1);
    static LightSensor sens2 = new LightSensor(SensorPort.S4);
    static int l=0, r=0;
			
    public static void getValue(){
    	l = sens2.getLightValue();
    	r = sens.getLightValue();
    }
}
