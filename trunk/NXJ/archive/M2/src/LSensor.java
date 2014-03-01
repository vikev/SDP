import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class LSensor {
		 		    
		 	static LightSensor lightL = new LightSensor(SensorPort.S4);
			static LightSensor lightR = new LightSensor(SensorPort.S1);
			
			public static boolean warning()
			{
				if (lightL.getLightValue() >= 50 || lightR.getLightValue() >= 50) return true;
				return false;
			}
			
			public static void main(String args[])
			{}
			
			
}
