import matrix.Matrix2D;

public class Main {
    public static void main(String[] args) {
        var m = new double[][]{
                {2, 2, 4},
                {2, 2, 6},
                {6, 8, 4}
        };
        var mat = new Matrix2D(m);
        var inv = mat.inverse();
        System.out.println(mat.toStringShort());
        System.out.println(inv.toStringShort());
    }
}
