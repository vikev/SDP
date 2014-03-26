package sdp.pc.vision;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Test;

/**
 * Test suite for methods in Alg
 */
public class AlgTest {
	private LinkedList<Point2> list;
	private Point2 pt;

	@Test
	public void testIsInHull() {

		// Inside basic hull from a 4 points
		list = new LinkedList<Point2>();
		list.add(new Point2(0, 0));
		list.add(new Point2(100, 0));
		list.add(new Point2(100, 100));
		list.add(new Point2(0, 100));
		pt = new Point2(50, 50);
		assertTrue(Alg.isInHull(list, pt));
	}

	@Test
	public void testInMinorHull() {

		// Inside basic minor hull
		list = new LinkedList<Point2>();
		list.add(new Point2(0, 0));
		list.add(new Point2(100, 0));
		list.add(new Point2(100, 100));
		list.add(new Point2(0, 100));
		pt = new Point2(50, 50);
		assertTrue(Alg.inMinorHull(list, 0.0, pt));

		// Not inside basic minor hull
		list = new LinkedList<Point2>();
		list.add(new Point2(0, 0));
		list.add(new Point2(100, 0));
		list.add(new Point2(100, 100));
		list.add(new Point2(0, 100));
		pt = new Point2(-50, -50);
		assertFalse(Alg.inMinorHull(list, 0.0, pt));

		// Inside expanded minor hull
		list = new LinkedList<Point2>();
		list.add(new Point2(0, 0));
		list.add(new Point2(100, 0));
		list.add(new Point2(100, 100));
		list.add(new Point2(0, 100));
		pt = new Point2(150, 150);
		assertTrue(Alg.inMinorHull(list, -100.0, pt));

		// Not inside contracted minor hull
		list = new LinkedList<Point2>();
		list.add(new Point2(0, 0));
		list.add(new Point2(100, 0));
		list.add(new Point2(100, 100));
		list.add(new Point2(0, 100));
		pt = new Point2(0, 0);
		assertFalse(Alg.inMinorHull(list, 50.0, pt));
	}
}
