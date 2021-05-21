
public class DoubleFactory2D {

    public static final DoubleFactory2D mat = new DoubleFactory2D();

    /**
     * Makes this class non instantiable, but still let's others inherit from
     * it.
     */
    protected DoubleFactory2D() {
    }

    /**
     * Constructs a matrix with uniformly distributed values in <tt>(0,1)</tt>
     * (exclusive).
     */
    public Matrix2D random(int rows, int columns) {
        return make(rows, columns).assign(a -> Math.random());
    }

    /**
     * Constructs a matrix with the given shape, each cell initialized with
     * zero.
     */
    public Matrix2D make(int rows, int columns) {
        return new Matrix2D(rows, columns);
    }
}
