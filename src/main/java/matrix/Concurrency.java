package matrix;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public abstract class Concurrency {


    protected static class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
        }
    }

    protected static class CustomThreadFactory implements ThreadFactory {
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

    public abstract Future<?> submit(Runnable task);

    public abstract int getMaxNumWorkers();
}
