package sdp.pc.vision;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Test;

public class AlgTest {

	@Test
	public void testInMinorHull() {

		// Inside hull
		LinkedList<Point2> list = new LinkedList<Point2>();
		list.add(new Point2(0, 0));
		list.add(new Point2(100, 0));
		list.add(new Point2(100, 100));
		list.add(new Point2(0, 100));
		Point2 pt = new Point2(50, 50);
		assertTrue(Alg.isInHull(list, pt));

		// Inside basic minor hull
		list = new LinkedList<Point2>();
		list.add(new Point2(0, 0));
		list.add(new Point2(100, 0));
		list.add(new Point2(100, 100));
		list.add(new Point2(0, 100));
		pt = new Point2(50, 50);
		assertTrue(Alg.inMinorHull(list, 0.0, pt));
	}

}
