import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.Motor;

public class TouchTest {
  public static void main(String[] args) throws Exception {
    TouchSensor touch = new TouchSensor(SensorPort.S1);
    while(true){
        if(touch.isPressed()){
            Motor.A.stop();
            Motor.B.stop();
        }else{
            Motor.A.backward();
            Motor.B.backward();
        }
    }
  }
}