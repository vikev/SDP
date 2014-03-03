package sdp.pc.robot.arbitrators;

import sdp.pc.robot.behaviours.Behavior;
import sdp.pc.robot.behaviours.DefendAttacker;
import sdp.pc.robot.behaviours.DefendBall;
import sdp.pc.robot.behaviours.MoveToGoalLine;
import sdp.pc.robot.behaviours.TurnPerpendicular;
import sdp.pc.robot.pilot.Robot;

/**
 * An arbiter for the defender robot. Adds the required defender behaviours on creation. 
 * Must be explicitly started.
 *
 * @author s1141301
 *
 */
public class DefenderArbiter extends Arbiter {
	
	/**
	 * Constructs a new <b>DefenderArbiter</b> using the default behaviours that constitute it. 
	 * @param r The robot for which to create the arbiter
	 */
	public DefenderArbiter(Robot robot) {
		super(new Behavior[] {
				new MoveToGoalLine(robot),
				new TurnPerpendicular(robot),
				new DefendBall(robot),
				new DefendAttacker(robot)
				//TODO pass the ball
		}, false);
	}
}
