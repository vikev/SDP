package sdp.pc.common;

public class GaussianAngleFilter implements Filter<Integer> {

	GaussianDoubleFilter sinFilter, cosFilter;
	
	public GaussianAngleFilter(int size) {
		sinFilter = new GaussianDoubleFilter(size);
		cosFilter = new GaussianDoubleFilter(size);
	}
	
	/**
	 * Applies the filter to the latest angle in the sequence
	 */
	@Override
	public Integer apply(Integer angle) {
		return (int)Math.toDegrees(Math.atan2(
				cosFilter.apply(Math.cos(Math.toRadians(angle))), 
				sinFilter.apply(Math.sin(Math.toRadians(angle)))));
	}

}
