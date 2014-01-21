import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;


public class UltrasonicControl {
	
	private UltrasonicSensor sensor;
	
	public UltrasonicControl(SensorPort port) {
		sensor = new UltrasonicSensor(port);
		sensor.ping();
	}

	/**
	 * The maximum readout value this sensor can return
	 */
	private static final int TRESHOLD = 50;
	
	/**
	 * the number of echoes sent by a ping() call
	 */
	private static final int ECHOES = 8;
	
	private int[] m_iMeasured = new int[ECHOES];
	
	
	public int getFastMeasurement() {
		sensor.ping();
		sensor.getDistances(m_iMeasured);
		
		return m_iMeasured[0];
//		int max = 0;
//		int accum = 0;
//		
//		//count how many are max'd, sum the others
//		for(int i = 0; i < ECHOES; i++) {
//			if(m_iMeasured[i] == 255) {
//				max++;
//			}
//			else {
//				accum += m_iMeasured[i];
//			}
//		}
		
//		//if more than half are max, return 255
//		if(max >= ECHOES / 2)
//			return 255;
//		//otherwise return sum divided by n of non-max elements
//		return accum / (ECHOES - max);
	}

}
