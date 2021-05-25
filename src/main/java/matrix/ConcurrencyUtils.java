package matrix;

import java.util.concurrent.*;

/**
 * This file is part of Parallel Colt library.
 */
public class ConcurrencyUtils {

    static Concurrency coroutineUtils = new CoroutineUtils();


    public static void useCoroutines() {
        coroutineUtils = new CoroutineUtils();
    }

    public static void useThreads() {
        coroutineUtils = new ThreadUtils();
    }

    public static void useUnlimitedThreads() {
        coroutineUtils = new UnlimitedThreadUtil();
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
        return coroutineUtils.submit(task);
    }

    /**
     * Returns the current number of threads.
     *
     * @return the current number of threads.
     */
    public static int getNumberOfThreads() {
        return coroutineUtils.getMaxNumWorkers();
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
