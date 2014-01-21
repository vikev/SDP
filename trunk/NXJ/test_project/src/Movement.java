package fromRepo;
import lejos.nxt.NXTRegulatedMotor; // Switch to using this instead of Motor
import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;

public class Movement {
	DifferentialPilot pilot;

	public Movement() {
		pilot = new DifferentialPilot(2.25f, 5.5f,(NXTRegulatedMotor) Motor.A,(NXTRegulatedMotor) Motor.C, true);
	}

	public void turn() {

	}
	
	// TODO Needs finer grained (maybe faster) left and right turns
	// TODO Refactor
	public void driveForward() {
		while (true) {
			System.out.println("Forward");
			pilot.setRotateSpeed(200);
			pilot.setTravelSpeed(8);
			while (ColorScanner.getNewDirection()==Direction.FORWARD) {
				pilot.forward();
			}
			switch(ColorScanner.getNewDirection()){
				case BACKWARD: 
					System.out.println("Backward");
					pilot.setTravelSpeed(5);
					while (ColorScanner.getNewDirection()!=Direction.BACKWARD) {
						pilot.backward();
					}
					break;
				case LEFT: 
					System.out.println("Left");
					pilot.rotate(5);
					break;
				case RIGHT:
					System.out.println("Right");
					pilot.rotate(-5);
					break;
			}
		}
	}

	public static void main(String args[]) {
		Movement move = new Movement();
		move.driveForward();
	}

}
