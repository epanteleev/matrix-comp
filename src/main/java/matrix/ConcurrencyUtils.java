package matrix;

import java.util.concurrent.*;

/**
 * This file is part of Parallel Colt library.
 */
public class ConcurrencyUtils {

    static ThreadUtils threadUtils = new ThreadUtils();

    static CoroutineUtils coroutineUtils = new CoroutineUtils();

    static boolean useCoro;

    public static void useCoroutines() {
        useCoro = true;
    }

    public static void useThreads() {
        useCoro = false;
    }

    /**
     * Returns the number of available processors
     *
     * @return number of available processors
     */
    public static int getNumberOfProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }
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
     * Submits a value-returning task for execution and returns a Future
     * representing the pending results of the task.
     *
     * @param <T>
     * @param task
     *            task for execution
     * @return a handle to the task submitted for execution
     */
    public static <T> Future<T> submit(Callable<T> task) {
        if (useCoro){
            return coroutineUtils.submit(task);
        } else {
            return threadUtils.submit(task);
        }
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
        if (useCoro){
            return coroutineUtils.submit(task);
        } else {
            return threadUtils.submit(task);
        }
    }

    /**
     * Returns the current number of threads.
     *
     * @return the current number of threads.
     */
    public static int getNumberOfThreads() {
        if (useCoro){
            return coroutineUtils.getMaxNumWorkers();
        } else {
            return threadUtils.getMaxNumWorkers();
        }
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
}
