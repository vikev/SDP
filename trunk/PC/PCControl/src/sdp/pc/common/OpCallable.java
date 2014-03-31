package sdp.pc.common;

import java.util.concurrent.Callable;

import sdp.pc.common.Parallel.Operation;

public abstract class OpCallable<T> implements Callable<Void> {

	public Iterable<T> elements;
	
	public OpCallable() { }
	
	public OpCallable(Iterable<T> elements) {
		this.elements = elements;
	}
}
