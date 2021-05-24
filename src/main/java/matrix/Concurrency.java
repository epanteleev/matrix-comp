package matrix;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class Concurrency {
    /**
     * Waits for all coroutines to complete computation.
     *
     * @param futures
     *            handles to running coroutines
     */

    public abstract  <T> Future<T> submit(Callable<T> task);

    public abstract Future<?> submit(Runnable task);

    public abstract int getMaxNumWorkers();
}
