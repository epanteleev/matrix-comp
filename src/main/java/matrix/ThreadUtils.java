package matrix;

import java.util.concurrent.*;

public class ThreadUtils {
    private ExecutorService THREAD_POOL;

    private static class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
        }
    }

    private static class CustomThreadFactory implements ThreadFactory {
        private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        private final Thread.UncaughtExceptionHandler handler;

        CustomThreadFactory(Thread.UncaughtExceptionHandler handler) {
            this.handler = handler;
        }

        public Thread newThread(Runnable r) {
            Thread t = defaultFactory.newThread(r);
            t.setUncaughtExceptionHandler(handler);
            t.setDaemon(true);
            return t;
        }
    };

    public ThreadUtils() {
        this.THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(
                new CustomExceptionHandler()));
    }

    /**
     * Submits a value-returning task for execution and returns a Future
     * representing the pending results of the task.
     *
     * @param <T>
     * @param task
     *            task for execution
     * @return a handle to the task submitted for execution
     */
    public <T> Future<T> submit(Callable<T> task) {
        if (THREAD_POOL.isShutdown() || THREAD_POOL.isTerminated()) {
            THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));
        }
        return THREAD_POOL.submit(task);
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
