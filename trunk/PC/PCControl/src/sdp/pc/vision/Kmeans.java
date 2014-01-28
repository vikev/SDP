package sdp.pc.vision;

import java.util.ArrayList;

public class Kmeans {

	public static final double errortarget = 200.0;

	public static Cluster doKmeans(ArrayList<Point2> points, Point2 mean1,
			Point2 mean2) {
		// All the information about this iteration is kept here.
		Cluster iteration = null;
		
		// We set the initial errors to some number that's not going to
		// interfere with our condition.
		Point2 mean1old = mean1;
		Point2 mean2old = mean2;
		Point2 mean1new = new Point2();
		Point2 mean2new = new Point2();
		int iterations = 0;

		// We iterate until we converge or until we get small enough of an error
		// for our clusters (:
		while ((mean1old != mean1new) && (mean2old != mean2new) && (iterations < 50)) {
			// Iteration is the holder of the current
			iteration = getClusters(points, mean1old, mean2old);
			// kmeans cluster output.
			mean1new = iteration.getMean(0);
			mean2new = iteration.getMean(1);

//			error1new = sumSquaredError(iteration.getCluster(0), mean1new);
//			error2new = sumSquaredError(iteration.getCluster(1), mean2new);
			mean1old = mean1new;
			mean2old = mean2new;
			++iterations;
		}
		return iteration;
	}

	// Position in the returned arraylist will correspond to the nth point in
	// the original array lists and will be either 1 or 2, depending of the
	// cluster
	public static Cluster getClusters(ArrayList<Point2> points,
			Point2 mean1, Point2 mean2) {
		// Clusters
		ArrayList<Point2> mean1members = new ArrayList<Point2>();
		ArrayList<Point2> mean2members = new ArrayList<Point2>();

		for (int i = 0; i < points.size(); i++) {
			Point2 p = points.get(i);

			double mean1dist = p.getDistance(mean1);
			double mean2dist = p.getDistance(mean2);

			// Add the points to the appropriate clusters.
			if (mean1dist < mean2dist)
				mean1members.add(p);
			else
				mean2members.add(p);
		}
		// Get the new means using our clusters.
		Point2 newmean1;
		if (mean1members.size() > 0)
			newmean1 = findMean(mean1members);
		else
			newmean1 = mean1;
		Point2 newmean2;
		if (mean2members.size() > 0)
			newmean2 = findMean(mean2members);
		else
			newmean2 = mean1;
		
		return new Cluster(mean1members, mean2members, newmean1, newmean2);
	}

	// Get the mean for the given points.
	// means is an array in the format {xcenter, ycenter};
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

	public static double sumSquaredError(ArrayList<Point2> points, Point2 center) {
		double sumSqErr = 0.0;

		for (int i = 0; i < points.size(); i++)
			sumSqErr += center.getDistance(points.get(i));
		
		return Math.sqrt(sumSqErr);
	}
}
