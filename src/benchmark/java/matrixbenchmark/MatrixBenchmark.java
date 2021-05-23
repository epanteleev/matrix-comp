package matrixbenchmark;

import matrix.ConcurrencyUtils;
import matrix.Matrix2D;
import org.openjdk.jmh.annotations.*;

import java.util.Random;

@State(Scope.Benchmark)
public class MatrixBenchmark {

    private static final Random random = new Random(0);
    /**
     * Matrix to test
     */
    private Matrix2D A;

    /**
     * Matrix of the same size as A
     */
    private Matrix2D B;

    @Setup(Level.Invocation)
    public void setUp() {
        createMatrices();
        populateMatrices();
    }

    private void populateMatrices() {
        ConcurrencyUtils.setThreadsBeginN_2D(ConcurrencyUtils.getNumberOfThreads());
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

    private void createMatrices() {
        int NROWS = 1300;
        int NCOLUMNS = 1700;
        A = new Matrix2D(NROWS, NCOLUMNS);
        B = new Matrix2D(NCOLUMNS, NROWS);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void matMull() {
        Matrix2D C = A.mult(B);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void coroutineMatMull() {
        Matrix2D C = A.cMult(B);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}