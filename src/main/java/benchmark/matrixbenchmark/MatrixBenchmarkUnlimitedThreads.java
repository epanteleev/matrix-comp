package benchmark.matrixbenchmark;

import matrix.ConcurrencyUtils;
import matrix.CoroutineUtils;
import matrix.Matrix2D;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 5, jvmArgs = {"-Xms2G", "-Xmx20G"})
public class MatrixBenchmarkUnlimitedThreads  extends MatrixBenchmark {

    @Setup(Level.Invocation)
    public void setUp() {
        super.setUp();
        ConcurrencyUtils.useUnlimitedThreads();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void matMullThread() {
        Matrix2D C = A.mult(B);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}

