import java.util.concurrent.Future;
import java.util.function.Function;

public class Matrix2D {

    protected double[] elements;

    /** the number of colums and rows this matrix (view) has */
    protected int columns, rows;

    /**
     * the number of elements between two rows, i.e.
     * <tt>index(i+1,j,k) - index(i,j,k)</tt>.
     */
    protected int rowStride;

    /**
     * the number of elements between two columns, i.e.
     * <tt>index(i,j+1,k) - index(i,j,k)</tt>.
     */
    protected int columnStride;

    /**
     * The index of the first element
     */
    protected int rowZero, columnZero;


    public Matrix2D(int rows, int columns) {
        setUp(rows, columns);
        this.elements = new double[rows * columns];
    }

    public void setQuick(int row, int column, double value) {
        this.elements[rowZero + row * rowStride + columnZero + column * columnStride] = value;
    }


    protected void setUp(int rows, int columns) {
        setUp(rows, columns, columns);
    }

    protected void setUp(int rows, int columns, int rowStride) {
        if (rows < 0 || columns < 0)
            throw new IllegalArgumentException("negative size");
        this.rows = rows;
        this.columns = columns;

        this.rowZero = 0;
        this.columnZero = 0;

        this.rowStride = rowStride;
        this.columnStride = 1;

        if ((double) columns * rows > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("matrix too large");
        }
    }

    public double getQuick(int row, int column) {
        return elements[rowZero + row * rowStride + columnZero + column * columnStride];
    }

    public Matrix2D like(int rows, int columns) {
        return new Matrix2D(rows, columns);
    }

    public long size() {
        return ((long) rows) * columns;
    }

    /**
     * Returns a string representation of the receiver's shape.
     */
    public String toStringShort() {
        return rows() + " x " + columns() + " matrix";
    }

    /**
     * Returns the number of rows.
     */
    public int rows() {
        return rows;
    }

    /**
     * Returns the number of columns.
     */
    public int columns() {
        return columns;
    }

    /**
     * Original DoubleMatrix2D
     */
    public Matrix2D mult(final Matrix2D B) {
        final int m = rows;
        final int n = columns;
        final int p = B.columns;
        final Matrix2D CC = like(m, p);

        if (B.rows != n)
            throw new IllegalArgumentException("Matrix2D inner dimensions must agree:" + toStringShort() + ", "
                    + B.toStringShort());
        if (CC.rows != m || CC.columns != p)
            throw new IllegalArgumentException("Incompatibe result matrix: " + toStringShort() + ", "
                    + B.toStringShort() + ", " + CC.toStringShort());
        if (this == CC || B == CC)
            throw new IllegalArgumentException("Matrices must not be identical");
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, p);
            Future<?>[] futures = new Future[nthreads];
            int k = p / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstIdx = j * k;
                final int lastIdx = (j == nthreads - 1) ? p : firstIdx + k;
                futures[j] = ConcurrencyUtils.submit(() -> {
                    for (int a = firstIdx; a < lastIdx; a++) {
                        for (int b = 0; b < m; b++) {
                            double s = 0;
                            for (int c = 0; c < n; c++) {
                                s += getQuick(b, c) * B.getQuick(c, a);
                            }
                            CC.setQuick(b, a, s);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int a = 0; a < p; a++) {
                for (int b = 0; b < m; b++) {
                    double s = 0;
                    for (int c = 0; c < n; c++) {
                        s += getQuick(b, c) * B.getQuick(c, a);
                    }
                    CC.setQuick(b, a, s);
                }
            }
        }
        return CC;
    }

    /**
     * Used coroutines for mult.
     */
    public Matrix2D cMult(final Matrix2D B) {
        final int m = rows;
        final int n = columns;
        final int p = B.columns;
        final Matrix2D CC = like(m, p);

        if (B.rows != n) {
            throw new IllegalArgumentException("Matrix2D inner dimensions must agree:" + toStringShort() + ", "
                    + B.toStringShort());
        }
        if (CC.rows != m || CC.columns != p) {
            throw new IllegalArgumentException("Incompatibe result matrix: " + toStringShort() + ", "
                    + B.toStringShort() + ", " + CC.toStringShort());
        }
        if (this == CC || B == CC) {
            throw new IllegalArgumentException("Matrices must not be identical");
        }
        int maxTaskSize = Math.min(CoroutineUtils.maxTaskSize(), p);

        Future<?>[] futures = new Future[maxTaskSize];
        int k = p / maxTaskSize;
        for (int j = 0; j < maxTaskSize; j++) {
            final int firstIdx = j * k;
            final int lastIdx = (j == maxTaskSize - 1) ? p : firstIdx + k;
            futures[j] = CoroutineUtils.submit(() -> {
                for (int a = firstIdx; a < lastIdx; a++) {
                    for (int b = 0; b < m; b++) {
                        double s = 0;
                        for (int c = 0; c < n; c++) {
                            s += getQuick(b, c) * B.getQuick(c, a);
                        }
                        CC.setQuick(b, a, s);
                    }
                }
            });
        }
        CoroutineUtils.waitForCompletion(futures);

        return CC;
    }

    /**
     * Constructs and returns a 2-dimensional array containing the cell values.
     * The returned array <tt>values</tt> has the form
     * <tt>values[row][column]</tt> and has the same number of rows and columns
     * as the receiver.
     * <p>
     * The values are copied. So subsequent changes in <tt>values</tt> are not
     * reflected in the matrix, and vice-versa.
     *
     * @return an array filled with the values of the cells.
     */
    public double[][] toArray() {
        final double[][] values = new double[rows][columns];
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            double[] currentRow = values[r];
                            for (int c = 0; c < columns; c++) {
                                currentRow[c] = getQuick(r, c);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                double[] currentRow = values[r];
                for (int c = 0; c < columns; c++) {
                    currentRow[c] = getQuick(r, c);
                }
            }
        }
        return values;
    }


    public Matrix2D assign(final Function<Double, Double> f) {
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        for (int r = firstRow; r < lastRow; r++) {
                            for (int c = 0; c < columns; c++) {
                                setQuick(r, c, f.apply(getQuick(r, c)));
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, f.apply(getQuick(r, c)));
                }
            }
        }
        return this;
    }
}
