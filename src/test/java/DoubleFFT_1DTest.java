import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import fft.CommonUtils;
import fft.DoubleFFT_1D;
import fft.IOUtils;
import matrix.ConcurrencyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.apache.commons.math3.util.FastMath.*;


@RunWith(value = Parameterized.class)
public class DoubleFFT_1DTest {

    /**
     * Base message of all exceptions.
     */
    public static final String DEFAULT_MESSAGE = "%d-threaded FFT of size %d: ";

    /**
     * Name of binary files (input, untransformed data).
     */
    private final static String FFTW_INPUT_PATTERN = "fftw%d.in";

    /**
     * Name of binary files (output, transformed data).
     */
    private final static String FFTW_OUTPUT_PATTERN = "fftw%d.out";

    /**
     * The constant value of the seed of the random generator.
     */
    public static final int SEED = 20110602;

    private static final double EPS = pow(10, -12);
    private final DoubleFFT_1D fft;

    @Parameters
    public static Collection<Object[]> getParameters() {
        final int[] size = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 32,
                64, 100, 120, 128, 256, 310, 512, 1024, 1056, 2048, 8192,
                10158, 16384, 32768, 65530, 65536, 131072};

        final ArrayList<Object[]> parameters = new ArrayList<Object[]>();
        for (int i = 0; i < size.length; i++) {
            parameters.add(new Object[]{size[i], 1, SEED});
            parameters.add(new Object[]{size[i], 2, SEED});
            parameters.add(new Object[]{size[i], 4, SEED});
        }
        return parameters;
    }

    /**
     * The size of the FFT to be tested.
     */
    private final int n;

    /**
     * The number of threads used.
     */
    private final int numThreads;

    /**
     * For the generation of the data arrays.
     */
    private final Random random;

    /**
     * Creates a new instance of this class.
     *
     * @param n          the size of the FFT to be tested
     * @param numThreads the number of threads
     * @param seed       the seed of the random generator
     */
    public DoubleFFT_1DTest(final int n, final int numThreads, final long seed) {
        this.n = n;
        this.fft = new DoubleFFT_1D(n);
        this.random = new Random(seed);
        CommonUtils.setThreadsBeginN_1D_FFT_2Threads(1024);
        CommonUtils.setThreadsBeginN_1D_FFT_4Threads(1024);
        ConcurrencyUtils.setNumberOfThreads(numThreads);
        this.numThreads = ConcurrencyUtils.getNumberOfThreads();
    }

    /**
     * Read the binary reference data files generated with FFTW. The structure
     * of these files is very simple: double values are written linearly (little
     * endian).
     *
     * @param name the file name
     * @param data the array to be updated with the data read (the size of this
     *             array gives the number of <code>double</code> to be retrieved
     */
    public void readData(final String name, final double[] data) {
        try {
            final File f = new File(getClass().getClassLoader()
                    .getResource(name).getFile());
            final FileInputStream fin = new FileInputStream(f);
            final FileChannel fc = fin.getChannel();
            final ByteBuffer buffer = ByteBuffer.allocate(8 * data.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            fc.read(buffer);
            for (int i = 0; i < data.length; i++) {
                data[i] = buffer.getDouble(8 * i);
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * This is a test of {@link DoubleFFT_1D#complexForward(double[])}. This
     * method is tested by computation of the FFT of some pre-generated data,
     * and comparison with results obtained with FFTW.
     */
    @Test
    public void testComplexForward() {
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        readData(String.format(FFTW_INPUT_PATTERN, n), actual);
        readData(String.format(FFTW_OUTPUT_PATTERN, n),
                expected);
        fft.complexForward(actual);
        double rmse = IOUtils.computeRMSE(actual, expected);
        Assert.assertEquals(String.format(DEFAULT_MESSAGE, numThreads, n) + ", rmse = " + rmse, 0.0, rmse, EPS);
    }

    /**
     * This is a test of {@link DoubleFFT_1D#complexInverse(double[], boolean)},
     * with the second parameter set to <code>true</code>.
     */
    @Test
    public void testComplexInverseScaled() {
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < 2 * n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[i] = actual[i];
        }
        fft.complexForward(actual);
        fft.complexInverse(actual, true);
        double rmse = IOUtils.computeRMSE(actual, expected);
        Assert.assertEquals(String.format(DEFAULT_MESSAGE, numThreads, n) + ", rmse = " + rmse, 0.0, rmse, EPS);
    }

    /**
     * This is a test of {@link DoubleFFT_1D#complexInverse(double[], boolean)},
     * with the second parameter set to <code>false</code>.
     */
    @Test
    public void testComplexInverseUnscaled() {
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < 2 * n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[i] = actual[i];
        }
        fft.complexForward(actual);
        fft.complexInverse(actual, false);
        final double s = 1. / (double) n;
        for (int i = 0; i < actual.length; i++) {
            actual[i] = s * actual[i];
        }
        double rmse = IOUtils.computeRMSE(actual, expected);
        Assert.assertEquals(String.format(DEFAULT_MESSAGE, numThreads, n) + ", rmse = " + rmse, 0.0, rmse, EPS);
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realForward(double[])}.
     */
    @Test
    public void testRealForward() {
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[2 * i] = actual[i];
            expected[2 * i + 1] = 0.;
        }
        fft.complexForward(expected);
        fft.realForward(actual);
        if (!CommonUtils.isPowerOf2(n)) {
            int m;
            if (n % 2 == 0) {
                m = n / 2;
            } else {
                m = (n + 1) / 2;
            }
            actual[n] = actual[1];
            actual[1] = 0;
            for (int k = 1; k < m; k++) {
                int idx1 = 2 * n - 2 * k;
                int idx2 = 2 * k;
                actual[idx1] = actual[idx2];
                actual[idx1 + 1] = -actual[idx2 + 1];
            }
        } else {
            for (int k = 1; k < n / 2; k++) {
                int idx1 = 2 * n - 2 * k;
                int idx2 = 2 * k;
                actual[idx1] = actual[idx2];
                actual[idx1 + 1] = -actual[idx2 + 1];
            }
            actual[n] = actual[1];
            actual[1] = 0;
        }

        double rmse = IOUtils.computeRMSE(actual, expected);
        Assert.assertEquals(String.format(DEFAULT_MESSAGE, numThreads, n) + ", rmse = " + rmse, 0.0, rmse, EPS);
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realForward(double[])}.
     */
    @Test
    public void testRealForwardFull() {
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[2 * i] = actual[i];
            expected[2 * i + 1] = 0.;
        }
        fft.complexForward(expected);
        fft.realForwardFull(actual);
        double rmse = IOUtils.computeRMSE(actual, expected);
        Assert.assertEquals(String.format(DEFAULT_MESSAGE, numThreads, n) + ", rmse = " + rmse, 0.0, rmse, EPS);
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realInverseFull(double[], boolean)}
     * , with the second parameter set to <code>true</code>.
     */
    @Test
    public void testRealInverseFullScaled() {
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[2 * i] = actual[i];
            expected[2 * i + 1] = 0.;
        }
        fft.realInverseFull(actual, true);
        fft.complexInverse(expected, true);
        double rmse = IOUtils.computeRMSE(actual, expected);
        Assert.assertEquals(String.format(DEFAULT_MESSAGE, numThreads, n) + ", rmse = " + rmse, 0.0, rmse, EPS);
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realInverseFull(double[], boolean)}
     * , with the second parameter set to <code>false</code>.
     */
    @Test
    public void testRealInverseFullUnscaled() {
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[2 * i] = actual[i];
            expected[2 * i + 1] = 0.;
        }
        fft.realInverseFull(actual, false);
        fft.complexInverse(expected, false);
        double rmse = IOUtils.computeRMSE(actual, expected);
        Assert.assertEquals(String.format(DEFAULT_MESSAGE, numThreads, n) + ", rmse = " + rmse, 0.0, rmse, EPS);
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realInverse(double[], boolean)},
     * with the second parameter set to <code>true</code>.
     */
    @Test
    public void testRealInverseScaled() {
        final double[] actual = new double[n];
        final double[] expected = new double[n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[i] = actual[i];
        }
        fft.realForward(actual);
        fft.realInverse(actual, true);
        double rmse = IOUtils.computeRMSE(actual, expected);
        Assert.assertEquals(String.format(DEFAULT_MESSAGE, numThreads, n) + ", rmse = " + rmse, 0.0, rmse, EPS);
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realInverse(double[], boolean)},
     * with the second parameter set to <code>false</code>.
     */
    @Test
    public void testRealInverseUnscaled() {
        final double[] actual = new double[n];
        final double[] expected = new double[n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[i] = actual[i];
        }
        fft.realForward(actual);
        fft.realInverse(actual, false);
        double s;
        if (CommonUtils.isPowerOf2(n) && n > 1) {
            s = 2. / (double) n;
        } else {
            s = 1. / (double) n;
        }
        for (int i = 0; i < actual.length; i++) {
            actual[i] = s * actual[i];
        }
        double rmse = IOUtils.computeRMSE(actual, expected);
        Assert.assertEquals(String.format(DEFAULT_MESSAGE, numThreads, n) + ", rmse = " + rmse, 0.0, rmse, EPS);
    }
}
