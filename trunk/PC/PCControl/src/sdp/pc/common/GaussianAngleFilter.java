package sdp.pc.common;

public class GaussianAngleFilter implements Filter<Double> {

	GaussianDoubleFilter sinFilter, cosFilter;
	boolean debug = false;
	
	public GaussianAngleFilter(int size, boolean debug) {
		sinFilter = new GaussianDoubleFilter(size);
		cosFilter = new GaussianDoubleFilter(size);
		this.debug = debug;
	}
	
	/**
	 * Applies the filter to the latest angle in the sequence
	 */
	@Override
	public Double apply(Double angle) {
		double ccos = Math.cos(Math.toRadians(angle));
		double ssin = Math.sin(Math.toRadians(angle));
		double cos = cosFilter.apply(ccos);
		double sin = sinFilter.apply(ssin);
		double ret = Math.toDegrees(Math.atan2(sin, cos));
		return ret;
	}

}
