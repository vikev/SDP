package sdp.pc.vision;

import java.util.ArrayList;

public class Cluster {
	private ArrayList<Point2> points;
	private Point2 mean;
	
	/**
	 * Constructs a new cluster, given an initial mean and no points in it
	 * @param mean its mean
	 */
	public Cluster(Point2 mean) {
		points = new ArrayList<Point2>();
		this.mean = mean;
	}
	
	/**
	 * Constructs a new clusters, given initial mean and some points in
	 * @param points
	 * @param mean
	 */
	public Cluster(ArrayList<Point2> points, Point2 mean) {
		this.points = points;
		this.mean = mean;
	}
	
	/**
	 * Adds the given point to the cluster. Does not update the mean
	 * @param p
	 */
	public void addPoint(Point2 p) {
		points.add(p);
	}

	/**
	 * Gets the amount of points in this cluster
	 * @return
	 */
	public int pointsCount() {
		return points.size();
	}
	
	/**
	 * Removes all points from this cluster
	 */
	public void clearPoints() {
		points.clear();
	}
	
	/**
	 * Gets the i'th point in the cluster
	 * @param i
	 * @return
	 */
	public Point2 getPoint(int i) {
		return points.get(i);
	}

	/**
	 * Returns the saved mean. May not be accurate if points have been added
	 * @return
	 */
	public Point2 getMean() {
		return mean;
	}

	/**
	 * Sets an updated value for the mean. 
	 * @param mean
	 */
	public void setMean(Point2 mean) {
		this.mean = mean;
	}
}
