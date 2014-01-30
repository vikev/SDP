package sdp.pc.vision;

import java.util.ArrayList;

public class Cluster {
	private ArrayList<Point2> points;
	private Point2 mean;
	
	public Cluster(Point2 mean) {
		points = new ArrayList<Point2>();
		this.mean = mean;
	}
	

	public Cluster(ArrayList<Point2> points, Point2 mean) {
		this.points = points;
		this.mean = mean;
	}
	
	public void addPoint(Point2 p) {
		points.add(p);
	}

	public int pointsCount() {
		return points.size();
	}
	
	public void clearPoints() {
		points.clear();
	}
	
	public Point2 getPoint(int i) {
		return points.get(i);
	}

	public Point2 getMean() {
		return mean;
	}

	public void setMean(Point2 mean) {
		this.mean = mean;
	}
}
