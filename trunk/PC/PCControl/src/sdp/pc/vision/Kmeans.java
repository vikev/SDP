package sdp.pc.vision;

import java.util.ArrayList;

/**
 * Class for calculating static KMeans data. Don't instantiated a KMeans.
 * 
 */
public class Kmeans {

	/**
	 * Maximum number of iterations to perform KMeans updating before short
	 * circuiting the loop
	 */
	private static final int MAX_ITERATIONS = 50;

	/**
	 * Does a k-means search on the given points with given k. The initial
	 * centres are chosen randomly.
	 * 
	 * @param points
	 *            the points to do a k-means on
	 * @param k
	 *            the amount of clusters
	 * @return a partitioning of the points in k clusters
	 */
	public static Cluster[] doKmeans(ArrayList<Point2> points, int k) {

		// Choose absolute binding values for initial max/min
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;

		// Find local max/min
		for (Point2 p : points) {
			if (p.getX() < minX)
				minX = p.getX();
			if (p.getX() > maxX)
				maxX = p.getX();
			if (p.getY() < minY)
				minY = p.getY();
			if (p.getY() > maxY)
				maxY = p.getY();
		}
		int dx = maxX - minX;
		int dy = maxY - minY;
		Point2[] centers = new Point2[k];

		// Fill array of centres
		for (int i = 0; i < k; i++)
			centers[i] = new Point2((int) (Math.random() * dx + minX),
					(int) (Math.random() * dy + minY));

		// Call recursive KMeans method
		return doKmeans(points, centers);
	}

	/**
	 * Does a k-means search on the given points, with given initial centres.
	 * The k value is determined by the amount of initial centres provided.
	 * 
	 * @param points
	 *            the points to do a k-means on
	 * @param means
	 *            the initial centres
	 * @return a partitioning of the points in k clusters
	 */
	public static Cluster[] doKmeans(ArrayList<Point2> points, Point2... means) {

		int k = means.length;

		int n = points.size();

		// cluster, means info
		Cluster[] clusters = new Cluster[k];
		for (int i = 0; i < k; i++)
			clusters[i] = new Cluster(means[i]);

		Point2[] newMeans = new Point2[k];

		int iterations = 0; // iterations so far

		double distChange; // the change in clusters' centre positions

		// We iterate until we converge or until we get small enough of an error
		// for our clusters (: (or too many instances occur)
		do {
			// reset clusters
			for (int i = 0; i < k; i++) {
				clusters[i].clearPoints();
				newMeans[i] = new Point2();
			}

			// update points' clusters
			for (int i = 0; i < n; i++) {
				Point2 p = points.get(i);

				// find the closest one
				int cluster = -1;
				double clusterDist = Integer.MAX_VALUE;
				for (int j = 0; j < k; j++) {
					double d = p.distance(clusters[j].getMean());
					if (d < clusterDist) { // smallest dist
						clusterDist = d;
						cluster = j;
					}
				}

				// add the point to it
				clusters[cluster].addPoint(p);
				newMeans[cluster] = newMeans[cluster].add(p);
			}

			// update cluster means, keep track of change
			distChange = 0;
			for (int i = 0; i < k; i++) {
				int cn = clusters[i].pointsCount();
				if (cn > 0) {
					Point2 newMean = newMeans[i].div(cn);
					distChange += newMean.distance(clusters[i].getMean());
					clusters[i].setMean(newMean);
				}
			}

			// Terminate iff the average distance is leq 1, or we reach
			// MAX_ITERATIONS
		} while (distChange > k && ++iterations < MAX_ITERATIONS);

		return clusters;
	}

	/**
	 * Get the mean from a list of points
	 * 
	 * @param points
	 * @return Point2 in the format of {xcenter, ycenter}
	 */
	public static Point2 findMean(ArrayList<Point2> points) {
		assert (points.size() > 0) : "Empty points list passed to findMean";

		int xSum = 0;
		int ySum = 0;
		for (int i = 0; i < points.size(); i++) {
			Point2 p = points.get(i);
			xSum += p.getX();
			ySum += p.getY();
		}
		int meanx = xSum / points.size();
		int meany = ySum / points.size();

		return new Point2(meanx, meany);
	}

	/**
	 * TODO: Doesn't seem to be in use, I believe. Remove if that is the case.
	 */
	// public static double sumSquaredError(ArrayList<Point2> points, Point2
	// center) {
	// double sumSqErr = 0.0;
	//
	// for (int i = 0; i < points.size(); i++)
	// sumSqErr += center.distance(points.get(i));
	//
	// return Math.sqrt(sumSqErr);
	// }
}
