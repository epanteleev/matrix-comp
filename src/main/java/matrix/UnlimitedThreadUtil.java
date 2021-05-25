package matrix;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UnlimitedThreadUtil extends Concurrency {

    private static final int TASK_SIZE = Integer.MAX_VALUE;

    private ExecutorService threadPool;

    public UnlimitedThreadUtil() {
        this.threadPool = Executors.newThreadExecutor(new CustomThreadFactory(
                new CustomExceptionHandler()));
    }

    public Future<?> submit(Runnable task) {
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            threadPool = Executors.newThreadExecutor(new CustomThreadFactory(
                    new CustomExceptionHandler()));
        }
        return threadPool.submit(task);
    }

    public int getMaxNumWorkers() {
        return TASK_SIZE;
    }
}
