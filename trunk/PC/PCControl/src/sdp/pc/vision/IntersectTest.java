package sdp.pc.vision;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntersectTest {

	private static Point2 a;
	private static Point2 b;
	private static int x;

	private static Intersect i;
	private static Point2 ball;
	private static Point2 estimate;

	@Test
	public void testGetEstimateIntersectX() {

		// Test case with 0 intersections
		ball = new Point2(50, 50);
		estimate = ball.copy();
		i = new Intersect(ball, estimate);
		assertEquals(i.getEstimateIntersectX(100), new Point2(100, 50));

		// Test case with 1 intersection in the first region
		ball = new Point2(50, 50);
		estimate = new Point2(200, 32);
		i = new Intersect(ball, estimate);
		i.addIntersection(new Point2(100, 100));
		assertEquals(i.getEstimateIntersectX(75), new Point2(75, 75));

		// Test case with two intersections in the second region
		ball = new Point2(0, 0);
		estimate = new Point2(300, 0);
		i = new Intersect(ball, estimate);
		i.addIntersection(new Point2(10, 100));
		i.addIntersection(new Point2(110, 0));
		assertEquals(i.getEstimateIntersectX(60), new Point2(60, 50));
		
		// Test case with three intersections in the final region
		ball = new Point2(0, 0);
		estimate = new Point2(300, 0);
		i = new Intersect(ball, estimate);
		i.addIntersection(new Point2(10, 100));
		i.addIntersection(new Point2(110, 0));
		i.addIntersection(new Point2(200,100));
		assertEquals(i.getEstimateIntersectX(250), new Point2(250, 50));
	}

	@Test
	public void testXIntersect() {

		// Test case on x-axis
		a = new Point2();
		b = new Point2(2, 0);
		x = 1;
		assertEquals(Intersect.xIntersect(x, a, b), new Point2(1, 0));

		// Test case on y-axis
		a = new Point2();
		b = new Point2(0, 2);
		x = 0;
		assertEquals(Intersect.xIntersect(x, a, b), new Point2());

		// Test basic case
		a = new Point2();
		b = new Point2(2, 2);
		x = 1;
		assertEquals(Intersect.xIntersect(x, a, b), new Point2(1, 1));

		// Test negative case
		a = new Point2(-3, 0);
		b = new Point2(-5, -2);
		x = -4;
		assertEquals(Intersect.xIntersect(x, a, b), new Point2(-4, -1));

		// Test case
		a = new Point2(50, 50);
		b = new Point2(100, 100);
		x = 75;
		assertEquals(Intersect.xIntersect(x, a, b), new Point2(75, 75));
	}
}
