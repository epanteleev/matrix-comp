package matrix;

import java.util.concurrent.Future;

public abstract class Concurrency {

    public abstract Future<?> submit(Runnable task);

    public abstract int getMaxNumWorkers();
}
