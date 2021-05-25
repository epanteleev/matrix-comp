package matrix;

import java.util.concurrent.*;

public class ThreadUtils extends Concurrency {
    private ExecutorService THREAD_POOL;

    public ThreadUtils() {
        this.THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(
                new CustomExceptionHandler()));
    }

    public Future<?> submit(Runnable task) {
        if (THREAD_POOL.isShutdown() || THREAD_POOL.isTerminated()) {
            THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));
        }
        return THREAD_POOL.submit(task);
    }

    public int getMaxNumWorkers() {
        return Runtime.getRuntime().availableProcessors();
    }
}
