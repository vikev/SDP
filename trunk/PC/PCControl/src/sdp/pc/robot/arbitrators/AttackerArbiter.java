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
				//TODO: add behaviours
				new GrabBall(robot),
				new KickToGoal(robot)
		}, false);
	}
}
