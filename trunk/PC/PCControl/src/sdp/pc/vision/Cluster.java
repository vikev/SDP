package sdp.pc.vision;

import java.util.ArrayList;

public class Cluster {
	private ArrayList<Point2>[] clusters;
	private Point2[] means;

	@SuppressWarnings("unchecked")
	public Cluster(ArrayList<Point2> cluster1, ArrayList<Point2> cluster2,
			Point2 mean1, Point2 mean2) {
		this.clusters = new ArrayList[] { cluster1, cluster2 };

		this.means = new Point2[] { mean1, mean2 };
	}

	// set and get the mean.

	public Point2 getMean(int index) {
		return this.means[index];
	}

	public void setMean(int index, Point2 mean) {
		this.means[index] = mean;
	}

	// Set and get where num could be 1 or 2 and letter x or y;

	public ArrayList<Point2> getCluster(int index) {
		return this.clusters[index];
	}

	public void setCluster(int index, ArrayList<Point2> cluster) {
		this.clusters[index] = cluster;
	}

}
