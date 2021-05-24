package fftbenchmark;

import fft.DoubleFFT_1D;
import fft.IOUtils;
import matrix.ConcurrencyUtils;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class FFTBenchmark {

    private DoubleFFT_1D fft;

    private double[] x;
    private static int sizes1D = 2_000_000;


    @Setup(Level.Invocation)
    public void setUp() {
        ConcurrencyUtils.useThreads();
        this.fft = new DoubleFFT_1D(sizes1D);
        this.x = new double[(int) (2 * sizes1D)];
        IOUtils.fillMatrix_1D(sizes1D, x);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void fttCalc() {
        fft.realForwardFull(x);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
