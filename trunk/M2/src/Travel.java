import lejos.nxt.LCD;
import lejos.nxt.TouchSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.robotics.navigation.DifferentialPilot;


public class Travel {
	
	static DifferentialPilot pilot;
	TouchSensor touch = new TouchSensor(SensorPort.S2);
	
	
	public void go()
	{
		while (!touch.isPressed()) 
		{
		    pilot.setTravelSpeed(10.0);  
		    System.out.println("Forward movement");
		    Direct.forward();
		    
			if (LSensor.warning())
		    {
				System.out.println("On white");
				Direct.stop();
		    }
			Direct.chooseDirection();
		}
		
	}
	
	public static void main(String[] args)
    {
        Travel traveler = new Travel();
        pilot = new DifferentialPilot(2.25f, 5.5f,(NXTRegulatedMotor) Motor.A,(NXTRegulatedMotor) Motor.C, true);
        traveler.go();
    }

}
