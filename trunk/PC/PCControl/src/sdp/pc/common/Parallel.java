package sdp.pc.common;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Parallel {
    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors() * 4;

    private static final ExecutorService forPool = Executors.newFixedThreadPool(NUM_CORES, Executors.defaultThreadFactory());


    public static <T> void For(final Collection<T>[] callables, Operation<T> op) {
    	
    	if(callables.length != NUM_CORES) {
    		System.err.println("Unable to parallelize the loop!");
    		return;
    	}
    	
        try {
            forPool.invokeAll(createCallables(callables, op));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private static <T> Collection<Callable<Void>> createCallables(final Collection<T>[] elems, final Operation<T> operation) {
        List<Callable<Void>> callables = new LinkedList<Callable<Void>>();
        
        for(int i = 0; i < NUM_CORES; i++)
        	callables.add(new OpCallable<T>(elems[i]) {
				@Override
				public Void call() throws Exception {
					for(final T e : elements)
						operation.perform(e);
					return null;
				}
			});
        return callables;
    }
    public static interface Operation<T> {
        public void perform(T pParameter);
    }
}