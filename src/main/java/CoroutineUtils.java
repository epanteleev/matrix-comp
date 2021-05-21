import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CoroutineUtils {

    private static final int TASK_SIZE = 10_000;

    private static ExecutorService CORO_POOL = Executors.newVirtualThreadExecutor();

    public static Future<?> submit(Runnable task) {
        if (CORO_POOL.isShutdown() || CORO_POOL.isTerminated()) {
            CORO_POOL = Executors.newVirtualThreadExecutor();
        }
        return CORO_POOL.submit(task);
    }

    /**
     * Waits for all coroutines to complete computation.
     *
     * @param futures
     *            handles to running coroutines
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

    public static int maxTaskSize() {
        return TASK_SIZE;
    }
}
