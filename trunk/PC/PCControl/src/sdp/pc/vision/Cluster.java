package sdp.pc.vision;

import java.util.ArrayList;

public class Cluster {
	private ArrayList<Position>[] clusters;
	private Position[] means;

	@SuppressWarnings("unchecked")
	public Cluster(ArrayList<Position> cluster1, ArrayList<Position> cluster2,
			Position mean1, Position mean2) {
		this.clusters = new ArrayList[] { cluster1, cluster2 };

		this.means = new Position[] { mean1, mean2 };
	}

	// set and get the mean.

	public Position getMean(int index) {
		return this.means[index];
	}

	public void setMean(int index, Position mean) {
		this.means[index] = mean;
	}

	// Set and get where num could be 1 or 2 and letter x or y;

	public ArrayList<Position> getCluster(int index) {
		return this.clusters[index];
	}

	public void setCluster(int index, ArrayList<Position> cluster) {
		this.clusters[index] = cluster;
	}

}
