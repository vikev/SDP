package sdp.pc.common;

/**
 * A generic filter interface. Can be applied on a stream of values to reduce different type of noise.
 * @author s1141301
 *
 * @param <T> The type of values to filter
 */
public interface Filter<T> {
	/**
	 * Applies this filter to the newly acquired value and returns the filtered value.
	 */
	public T apply(T newVal);
}
