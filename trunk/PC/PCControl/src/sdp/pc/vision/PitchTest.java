package sdp.pc.vision;

import static org.junit.Assert.*;

import org.junit.Test;

import sdp.pc.robot.pilot.Strategy;

public class PitchTest {

	private static final int GOAL_RANDOM_COUNT = 100;

	@Test
	public void testGetLeftGoalRandom() throws InterruptedException {
		Strategy s = new Strategy();
		Thread.sleep(3000);
		WorldState st = s.getState();
		Pitch p = st.getPitch();
		Thread.sleep(3000);
		
		// Test points inside the goal
		for (int i = 0; i < GOAL_RANDOM_COUNT; i++) {
			int y = p.getLeftGoalRandom(1).getY();
			assertTrue(y < p.goalLineY[1] && y > p.goalLineY[0]);
		}
	}

}
