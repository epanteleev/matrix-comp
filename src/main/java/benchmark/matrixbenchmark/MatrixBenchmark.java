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
public class MatrixBenchmark {

    protected static Random random;
    /**
     * Matrix to test
     */
    protected Matrix2D A;

    /**
     * Matrix of the same size as A
     */
    protected Matrix2D B;

    @Setup(Level.Invocation)
    public void setUp() {
        ConcurrencyUtils.useThreads();
        random = new Random(0);
        createMatrices();
        populateMatrices();
    }

    protected void populateMatrices() {
        for (int r = 0; r < A.rows(); r++) {
            for (int c = 0; c < A.columns(); c++) {
                A.setQuick(r, c, random.nextDouble());
            }
        }

        for (int r = 0; r < B.rows(); r++) {
            for (int c = 0; c < B.columns(); c++) {
                B.setQuick(r, c, random.nextDouble());
            }
        }
    }

    protected void createMatrices() {
        int NROWS = 6000;
        int NCOLUMNS = 7000;
        A = new Matrix2D(NROWS, NCOLUMNS);
        B = new Matrix2D(NCOLUMNS, NROWS);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    final public void matMull() {
        Matrix2D C = A.mult(B);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
