package sdp.pc.robot.arbitrators;

import sdp.pc.robot.behaviours.*;
import sdp.pc.robot.pilot.Robot;

/**
 * An arbiter for the defender robot. Adds the required defender behaviours on creation. 
 * Must be explicitly started.
 *
 * @author s1141301
 *
 */
public class AttackerArbiter extends Arbiter {
	
	/**
	 * Constructs a new <b>AttackerArbiter</b> using the default behaviours that constitute it. 
	 * @param r The robot for which to create the arbiter
	 */
	public AttackerArbiter(Robot robot) {
		super(new Behavior[] {
				new DoNothing(robot),
				new DefendRobot(robot),
				new DefendBall(robot),
				new TurnPerpendicular(robot),
				new MoveToBall(robot),
				new KickToGoal(robot),
				new StayInside(robot),
		}, false);
	}
}
