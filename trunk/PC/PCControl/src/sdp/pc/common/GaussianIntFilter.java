package sdp.pc.common;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A gaussian filter working on integers.
 * 
 * @author s1141301
 * 
 */
public class GaussianIntFilter implements Filter<Integer> {

	/**
	 * A main method used for testing the behaviour of the gaussian filter (not
	 * actively used)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		GaussianIntFilter f = new GaussianIntFilter(15);

		while (true) {
			int val = 20 + (int) (Math.random() * 10 + 0.5);
			System.out.printf("%d\t%d\n", f.apply(val), val);
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
	private LinkedList<Integer> hist = new LinkedList<Integer>();

	/**
	 * Constructs a new gaussian int filter of the given size.
	 * 
	 * @param size
	 *            the kernel size
	 */
	public GaussianIntFilter(int size) {
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
	public Integer apply(Integer newVal) {
		hist.addFirst(newVal);
		if (hist.size() < size)
			return newVal;
		if (hist.size() > size)
			hist.removeLast();

		double sum = 0;
		int i = 0;
		Iterator<Integer> it = hist.iterator();
		while (it.hasNext()) {
			sum += kernel[i++] * it.next();
		}

		return (int) (sum + 0.5);
	}
}
