package sdp.pc.common;

import sdp.pc.vision.Point2;

/**
 * A gaussian filter working on Point2s
 * 
 * @author s1141301
 *
 */
public class GaussianPointFilter implements Filter<Point2> {
	GaussianIntFilter xFilter, yFilter;

	/**
	 * Constructs a new gaussian Point2 filter of the given size. 
	 * @param size the kernel size
	 */
	public GaussianPointFilter(int size) {
		xFilter = new GaussianIntFilter(size);
		yFilter = new GaussianIntFilter(size);
	}
	
	@Override
	public Point2 apply(Point2 p) {
		return new Point2(xFilter.apply(p.x), yFilter.apply(p.y));
	}

}
