package sdp.pc.common;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A gaussian filter working on doubles.
 * 
 * @author s1141301
 * 
 */
public class GaussianDoubleFilter implements Filter<Double> {

	/**
	 * A main method used for testing the behaviour of the gaussian filter (not
	 * actively used)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		GaussianDoubleFilter f = new GaussianDoubleFilter(15);

		while (true) {
			double val = 20.0 + (Math.random() * 10.0 + 0.5);
			System.out.println(f.apply(val));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private final double sqrt2pi = Math.sqrt(2 * Math.PI);

	/**
	 * The (gaussian) kernel applied to the last couple of values.
	 */
	private double[] kernel;

	/**
	 * The size of the kernel
	 */
	private int size;

	/**
	 * The past <i>size</i> points from the stream
	 */
	private LinkedList<Double> hist = new LinkedList<Double>();

	/**
	 * Constructs a new gaussian double filter of the given size.
	 * 
	 * @param size
	 *            the kernel size
	 */
	public GaussianDoubleFilter(int size) {
		int std = size / 3;

		this.size = size;
		
		double sum = 0;
		kernel = new double[size];
		
		for (int i = 0; i < size; i++) {
			kernel[i] = gaussian(i, std);
			sum += kernel[i];
		}
		
		for (int i = 0; i < size; i++)
			kernel[i] /= sum;
	}

	/**
	 * self-explanatory..
	 * 
	 * @param x
	 *            The X of the point
	 * @param std
	 *            The standard deviation of the gaussian
	 * @return The value of the gaussian at this point.
	 */
	private double gaussian(int x, int std) {
		return Math.exp(-0.5 * x * x / std / std) / sqrt2pi / std;
	}

	@Override
	public Double apply(Double newVal) {
		hist.addFirst(newVal);
		if (hist.size() < size)
			return newVal;
		if (hist.size() > size)
			hist.removeLast();

		double sum = 0;
		int i = 0;
		
		Iterator<Double> it = hist.iterator();
		while (it.hasNext()) {
			sum += kernel[i++] * it.next();
		}

		return sum;
	}
}
