package matrix;

import java.util.concurrent.Future;

public class Matrix2D {

    protected double[] elements;

    /**
     * the number of colums and rows this matrix (view) has
     */
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

    public Matrix2D(double[][] values) {
        this(values.length, values.length == 0 ? 0 : values[0].length);
        assign(values);
    }

    public void setQuick(int row, int column, double value) {
        this.elements[rowZero + column * rowStride + columnZero + row * columnStride] = value;
    }

    public double getQuick(int row, int column) {
        return elements[rowZero + column * rowStride + columnZero + row * columnStride];
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

    public Matrix2D like(int rows, int columns) {
        return new Matrix2D(rows, columns);
    }

    public Matrix2D like() {
        return like(rows, columns);
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
            throw new IllegalArgumentException("matrix.Matrix2D inner dimensions must agree:" + toStringShort() + ", "
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
            throw new IllegalArgumentException("matrix.Matrix2D inner dimensions must agree:" + toStringShort() + ", "
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

    public boolean isSquare() {
        return rows == columns;
    }

    public boolean isDiagonal() {
        double epsilon = Property.tolerance();
        for (int row = rows(); --row >= 0; ) {
            for (int column = columns(); --column >= 0; ) {
                if (row != column && !(Math.abs(getQuick(row, column)) <= epsilon)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkShape(Matrix2D B) {
        if (columns != B.columns || rows != B.rows) {
            throw new IllegalArgumentException("Incompatible dimensions: " + toStringShort() + " and "
                    + B.toStringShort());
        }
    }

    /**
     * Returns <tt>true</tt> if both matrices share at least one identical cell.
     */
    protected boolean haveSharedCells(Matrix2D other) {
        if (other == null) {
            return false;
        } else {
            return this == other;
        }
    }

    public Matrix2D assign(Matrix2D other) {
        if (other == this) {
            return this;
        }
        checkShape(other);
        final Matrix2D source;
        if (haveSharedCells(other)) {
            source = other.copy();
        } else {
            source = other;
        }
        int nthreads = ConcurrencyUtils.getNumberOfThreads();
        if ((nthreads > 1) && (rows * columns >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(() -> {
                    for (int r = firstRow; r < lastRow; r++) {
                        for (int c = 0; c < columns; c++) {
                            setQuick(r, c, source.getQuick(r, c));
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    setQuick(r, c, source.getQuick(r, c));
                }
            }
        }
        return this;
    }

    public long index(int row, int column) {
        return rowZero + (long) row * rowStride + columnZero + (long) column * columnStride;
    }

    public void assign(final double[][] values) {
        if (values.length != rows)
            throw new IllegalArgumentException("Must have same number of rows: rows=" + values.length + "rows()="
                    + rows());
        int nthreads = ConcurrencyUtils.getNumberOfThreads();

        final int zero = (int) index(0, 0);
        if ((nthreads > 1) && (size() >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            nthreads = Math.min(nthreads, rows);
            Future<?>[] futures = new Future[nthreads];
            int k = rows / nthreads;
            for (int j = 0; j < nthreads; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == nthreads - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(() -> {
                    int idx = zero + firstRow * rowStride;
                    for (int r = firstRow; r < lastRow; r++) {
                        double[] currentRow = values[r];
                        if (currentRow.length != columns)
                            throw new IllegalArgumentException("Must have same number of columns in every row: columns="
                                            + currentRow.length + "columns()=" + columns());
                        for (int i = idx, c = 0; c < columns; c++) {
                            elements[i] = currentRow[c];
                            i += columnStride;
                        }
                        idx += rowStride;
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            int idx = zero;
            for (int r = 0; r < rows; r++) {
                double[] currentRow = values[r];
                if (currentRow.length != columns)
                    throw new IllegalArgumentException("Must have same number of columns in every row: columns="
                            + currentRow.length + "columns()=" + columns());
                for (int i = idx, c = 0; c < columns; c++) {
                    elements[i] = currentRow[c];
                    i += columnStride;
                }
                idx += rowStride;
            }
        }
    }

    /**
     * @return a deep copy of the receiver.
     */
    public Matrix2D copy() {
        return like().assign(this);
    }

    public Matrix2D inverse() {
        Matrix2D inv = copy();
        if (isSquare() && isDiagonal()) {
            boolean isNonSingular = true;
            for (int i = inv.rows(); --i >= 0; ) {
                double v = inv.getQuick(i, i);
                isNonSingular &= (v != 0);
                inv.setQuick(i, i, 1 / v);
            }
            if (!isNonSingular) {
                throw new IllegalArgumentException("A is singular.");
            }
        } else {
            Matrix2D lu = copy();
            int[] p = Algorithm.LUPDecomposition(lu);
            Algorithm.LUPInvert(inv, lu, p);
        }
        return inv;
    }

}
