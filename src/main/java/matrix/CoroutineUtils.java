package matrix;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CoroutineUtils extends Concurrency {

    private static final int TASK_SIZE = Integer.MAX_VALUE;

    private ExecutorService coroPool;

    public CoroutineUtils() {
        this.coroPool = Executors.newVirtualThreadExecutor();
    }

    public Future<?> submit(Runnable task) {
        if (coroPool.isShutdown() || coroPool.isTerminated()) {
            coroPool = Executors.newVirtualThreadExecutor();
        }
        return coroPool.submit(task);
    }

    public int getMaxNumWorkers() {
        return TASK_SIZE;
    }
}
