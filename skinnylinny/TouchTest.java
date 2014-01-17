import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.Motor;

public class TouchTest {
  public static void main(String[] args) throws Exception {
    Motor.A.backward();
    Motor.C.backward();
    TouchSensor touch = new TouchSensor(SensorPort.S1);
    while (!touch.isPressed()) {
    	// try again
    }
    Motor.A.stop();
    Motor.C.stop();
    LCD.drawString("Finished", 3, 4);
  }
}