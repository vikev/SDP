package sdp.pc.vision;

import static org.junit.Assert.*;

import org.junit.Test;

public class Point2Test {

	/**
	 * Equivalent to 1/1000 as a double (used for double epsilon thresholding)
	 */
	private static final double MILLI = 1.0 / 1000.0;

	/**
	 * Method for testing the correctness of Point2.angleBetween()
	 */
	@Test
	public void testAngleBetween() {

		// Test simple QI case
		Point2 a = new Point2(0, 1);
		Point2 b = new Point2(1, 0);
		assertTrue(a.angleBetween(b) == 90.0);

		// Test more complex QIII case
		a = new Point2(-1, 0);
		b = new Point2(-1, -1);
		int q = Alg.doubleComparator(a.angleBetween(b), 45.0, MILLI);
		assertTrue(q == 0);

		// Perpendicular line case
		a = new Point2(500, 0);
		b = new Point2(-10000, 0);
		q = Alg.doubleComparator(a.angleBetween(b), 180.0, MILLI);
		assertTrue(q == 0);

		// Origin vector case
		a = new Point2(0, 0);
		b = new Point2(1, 0);
		assertTrue(Double.compare(a.angleBetween(b), Double.NaN) == 0);
	}
}
