package sdp.pc.vision;

import java.awt.geom.Point2D;

public class Circle {
	public Point2D.Double position;
	public double radius;
	public Circle(Point2D.Double pos, double radius) {
		this.position = pos;
		this.radius = radius;
	}
	
	public Circle(Point2 a, Point2 b, Point2 c) {
		b = b.subtract(a);
		c = c.subtract(a);
		Point2 lb = b.div(2);
		Point2 lc = c.div(2);
		
		this.position = a.add(Point2.getLinesIntersection(b, c, lb, lc));
		this.radius = a.distance(position);
	}
	
	public boolean isPointInside(Point2 p) {
		return p.distance(position) <= radius;
	}
}
