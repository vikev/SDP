package sdp.pc.vision;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntersectTest {

	private static Point2 a;
	private static Point2 b;
	private static int x;

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
		System.out.println(Intersect.xIntersect(x, a, b));
		assertEquals(Intersect.xIntersect(x, a, b), new Point2(-4, -1));
	}

}
