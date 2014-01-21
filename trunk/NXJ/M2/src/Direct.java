
public class Direct {
	public static void forward()
	{
		Travel.pilot.forward();
	}
	public static void backward()
	{
		Travel.pilot.backward();
	}
	public static void stop()
	{
		Travel.pilot.stop();
	}
	public static void rotate(double angle) 
	{
		Travel.pilot.rotate(angle);
	}
	
	public static void chooseDirection()
	{
		if (LSensor.lightL.getLightValue() >= 50 && LSensor.lightR.getLightValue() >= 50)
		{
			System.out.println("Backward movement");
			backward();
			Travel.pilot.travel(10);
			rotate(110);
		}
		else
		if (LSensor.lightL.getLightValue() >= 50)
		{
			System.out.println("Rotate right");
			rotate(-12);
		}
		else
		if (LSensor.lightR.getLightValue() >= 50)
		{
			System.out.println("Rotate left");
			rotate(12);
		}
	}
	
	public static void main(String args[])
	{}
}
