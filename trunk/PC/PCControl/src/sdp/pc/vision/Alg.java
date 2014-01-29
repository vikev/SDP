package sdp.pc.vision;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Alg {

	/**
	 * Computes the smallest circle that contains the given points
	 * @param pts the points to enclose
	 * @return the minimum-radius circle enclosing them
	 */
	public static Circle getSmallestCircle(ArrayList<Point2> pts) {
		return minCircle(pts.size(), pts, 0, new Point2[3]);
	}

	
	/**
	 * Computes the smallest circle enclosing the n points in p
	 * such that the m points in B are on its boundary
	 * @param n the amount of points in p
	 * @param p the points enclosed in the circle
	 * @param m the amount of points in b
	 * @param b the points on the circle boundary
	 * @return
	 */
	private static Circle minCircle(int n, ArrayList<Point2> p, int m, Point2[] b) {
		Circle minC;
		switch(m) {
		case 0:
			minC = new Circle(new Point2D.Double(-1, -1), 0);
			break;
		case 1:
			minC = new Circle(b[0].toDouble(), 0);
			break;
		case 2:
			minC = new Circle(b[0].add(b[1]).div(2).toDouble(), b[0].getDistance(b[1]) / 2);
			break;
		default:
			return new Circle(b[0], b[1], b[2]);
		}
		
		//... Now see if all the points in P are enclosed.
		for(int i = 0; i < n; i++)
		    if(p.get(i).getDistance(minC.position) > minC.radius )
			{
			    //... Compute B <--- B union P[i].
			    b[m] = new Point2(p.get(i));

			    //... Recurse
			    minC = minCircle(i, p, m+1, b);
			}
		return minC;



	}

	
}
