package matrix;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CoroutineUtils {

    private static final int TASK_SIZE = 10_000;

    private ExecutorService CORO_POOL;

    public CoroutineUtils() {
        this.CORO_POOL = Executors.newVirtualThreadExecutor();
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (CORO_POOL.isShutdown() || CORO_POOL.isTerminated()) {
            CORO_POOL = Executors.newVirtualThreadExecutor();
        }
        return CORO_POOL.submit(task);
    }

    public Future<?> submit(Runnable task) {
        if (CORO_POOL.isShutdown() || CORO_POOL.isTerminated()) {
            CORO_POOL = Executors.newVirtualThreadExecutor();
        }
        return CORO_POOL.submit(task);
    }

    public int getMaxNumWorkers() {
        return TASK_SIZE;
    }
}
