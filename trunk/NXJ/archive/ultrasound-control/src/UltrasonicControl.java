import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;


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
	private static final int ECHOS = 8;
	
	private int[] m_iMeasured = new int[ECHOS];

	public double getSuggestedRotateAngle(DifferentialPilot pilot, int angle, int tries) {
		return 0;
	}
	
	public double getSuggestedRotateAngle(DifferentialPilot pilot, int angle) {
		int[] vals = getAngledDistances(pilot, angle);
		if(vals[0] == vals[2])
			return 90;
		if(vals[0] > vals[2])	//left is further than right => turn left
			return shanoFunc(angle, vals[1], vals[0]);
		return -shanoFunc(angle, vals[1], vals[2]);
	}
	
	private static double cosineLaw(int angleDeg, int a, int b) {
		double angleRad = (double)angleDeg * Math.PI / 180.0;
		return Math.sqrt(a*a + b*b - 2*a*b*Math.cos(angleRad));
	}
	
	private static double shanoFunc(int angleDeg, int a, int b) {
		return (a * 180 + angleDeg * b) / (a + b);
	}
	
	public int[] getAngledDistances(DifferentialPilot pilot) {
		return getAngledDistances(pilot, 15);
	}
	
	public int[] getAngledDistances(DifferentialPilot pilot, int angle) {
		return getAngledDistances(pilot, angle, new int[3]);
	}
	public int[] getAngledDistances(DifferentialPilot pilot, int angle, int[] prevVals) {

		//const
		int sleepTime = 350;
		
		//action info
		LCD.clear(0);
		LCD.drawString("Finding direction", 0, 0);
		
		//get central dist
		prevVals[1] += getFastMeasurement();
		LCD.drawInt(prevVals[1], 5, 1);
		
		//rotate, ping, wait a bit
		pilot.rotate(angle);
		
		//read left distance
		prevVals[0] += getFastMeasurement();
		LCD.drawInt(prevVals[0], 0, 1);

		//rotate, ping, wait a bit
		pilot.rotate(-2*angle);
		
		//read right distance
		prevVals[2] += getFastMeasurement();
		LCD.drawInt(prevVals[2], 10, 1);
		
		pilot.rotate(angle);

		return prevVals;
	}
	
	public int getFastMeasurement() {
		sensor.ping();
		for(int i = 0; i < ECHOS; i++)
			m_iMeasured[i] = -1;
		sensor.getDistances(m_iMeasured);
		
//		int count = ECHOS,
//			sum = 0;
//		for(int i = 0; i < ECHOS; i++)
//			if(m_iMeasured[i] == -1)
//				count--;
//			else
//				sum += m_iMeasured[i];
//		
//		int avg = sum / count;
//		
//		LCD.clear();
//		LCD.drawString("measurements:", 0, 0);
//		LCD.drawString(m_iMeasured[0] + " " + m_iMeasured[1] + " " + m_iMeasured[2] + " " + m_iMeasured[3], 0, 1);
//		LCD.drawString(m_iMeasured[4] + " " + m_iMeasured[5] + " " + m_iMeasured[6] + " " + m_iMeasured[7], 0, 2);
//		LCD.drawInt(avg, 0, 3);
//		Button.waitForAnyPress();
		
		
		return m_iMeasured[0];
	}

}
