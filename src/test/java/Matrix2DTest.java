
import java.util.Random;

import junit.framework.TestCase;

/**
 * See DoubleMatrix2DTest
 */
public class Matrix2DTest extends TestCase {

    /**
     * Matrix to test
     */
    protected Matrix2D A;

    /**
     * Matrix of the same size as A
     */
    protected Matrix2D B;

    /**
     * Matrix of the size A.columns() x A.rows()
     */
    protected Matrix2D Bt;

    protected int NROWS = 1300;

    protected int NCOLUMNS = 1700;

    protected double TOL = 1e-10;

    protected static final Random random = new Random(0);

    protected void setUp() throws Exception {
        createMatrices();
        populateMatrices();
    }

    protected void createMatrices() throws Exception {
        A = new Matrix2D(NROWS, NCOLUMNS);
        B = new Matrix2D(NCOLUMNS, NROWS);
        Bt = new Matrix2D(NCOLUMNS, NROWS);
    }

    protected void populateMatrices() {
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

        for (int r = 0; r < Bt.rows(); r++) {
            for (int c = 0; c < Bt.columns(); c++) {
                Bt.setQuick(r, c, random.nextDouble());
            }
        }
    }

    private void check(double[][] expected, Matrix2D mat) {
        for (int r = 0; r < A.rows(); r++) {
            for (int c = 0; c < A.rows(); c++) {
                assertEquals(expected[r][c], mat.getQuick(r, c), TOL);
            }
        }
    }

    private double[][] makeExpect(Matrix2D A, Matrix2D B) {
        double[][] expected = new double[A.rows()][A.rows()];
        for (int j = 0; j < A.rows(); j++) {
            for (int i = 0; i < A.rows(); i++) {
                double s = 0;
                for (int k = 0; k < A.columns(); k++) {
                    s += A.getQuick(i, k) * B.getQuick(k, j);
                }
                expected[i][j] = s;
            }
        }
        return expected;
    }

    public void testMult() {
        double[][] expected = makeExpect(A, Bt);
        Matrix2D C = A.mult(Bt);
        check(expected, C);

        //---
        C = A.mult(B);
        expected = makeExpect(A, B);
        check(expected, C);
    }

    public void testMultCoro() {
        double[][] expected = makeExpect(A, Bt);
        Matrix2D C = A.cMult(Bt);
        check(expected, C);

        //---
        C = A.cMult(B);
        expected = makeExpect(A, B);
        check(expected, C);
    }
}
