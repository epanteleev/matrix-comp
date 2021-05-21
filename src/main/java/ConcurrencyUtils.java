import java.util.concurrent.*;

/**
 * This file is part of Parallel Colt library.
 */
public class ConcurrencyUtils {

    private static ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(
            new CustomExceptionHandler()));

    private static int NTHREADS = getNumberOfProcessors();

    private static int THREADS_BEGIN_N_2D = Integer.MAX_VALUE;

    /**
     * Returns the number of available processors
     *
     * @return number of available processors
     */
    public static int getNumberOfProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

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

    /**
     * Causes the currently executing thread to sleep (temporarily cease
     * execution) for the specified number of milliseconds.
     *
     * @param millis
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shutdowns the thread pool.
     */
    public static void shutdown() {
        THREAD_POOL.shutdown();
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
    public static <T> Future<T> submit(Callable<T> task) {
        if (THREAD_POOL.isShutdown() || THREAD_POOL.isTerminated()) {
            THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));
        }
        return THREAD_POOL.submit(task);
    }

    /**
     * Submits a Runnable task for execution and returns a Future representing
     * that task.
     *
     * @param task
     *            task for execution
     * @return a handle to the task submitted for execution
     */
    public static Future<?> submit(Runnable task) {
        if (THREAD_POOL.isShutdown() || THREAD_POOL.isTerminated()) {
            THREAD_POOL = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomExceptionHandler()));
        }
        return THREAD_POOL.submit(task);
    }

    /**
     * Returns the current number of threads.
     *
     * @return the current number of threads.
     */
    public static int getNumberOfThreads() {
        return NTHREADS;
    }

    /**
     * Waits for all threads to complete computation.
     *
     * @param futures
     *            handles to running threads
     */
    public static void waitForCompletion(Future<?>[] futures) {
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void setNumberOfThreads(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be greater or equal 1");
        }
        NTHREADS = n;
    }

    public static void setThreadsBeginN_2D(int n) {
        THREADS_BEGIN_N_2D = n;
    }

    /**
     * Returns the minimal size of 2D data for which threads are used.
     *
     * @return the minimal size of 2D data for which threads are used
     */
    public static int getThreadsBeginN_2D() {
        return THREADS_BEGIN_N_2D;
    }
}
