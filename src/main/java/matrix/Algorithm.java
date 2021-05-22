package matrix;

public class Algorithm {

    public static int getItself(int itself, int dummy) {
        return itself;
    }

    public static int[] LUPDecomposition(Matrix2D A) {
        int n = A.rows();
        int[] pi = new int[A.rows()];

        for (int i = 0; i < n; i++) {
            pi[i] = i + 1;
        }

        for (int i = 0; i < n; i++) {
            double pivot = 0.0;
            int imax = 0;

            for (int k = i; k < n; k++) {
                if (Math.abs(A.getQuick(k, i)) > pivot) {
                    pivot = Math.abs(A.getQuick(k, i));
                    imax = k;
                }
            }

            if (pivot < Property.tolerance()) {
                throw new InternalError();
            }

            if (imax != i) {
                // swap pi[k] <-> pi[k1]
                var tmp = pi[i];
                pi[i] = pi[imax];
                pi[imax] = tmp;

                for (int j = 0; j < n; j++) {
                    // swap A[i,j] <-> a[imax, j]
                    double val = A.getQuick(i, j);
                    A.setQuick(i, j, A.getQuick(imax, j));
                    A.setQuick(imax, j, val);
                }
            }

            for (int j = i + 1; j < n; j++) {
                double val = A.getQuick(j, i);
                A.setQuick(j, i, A.getQuick(i, i) / val);

                for (int k = i + 1; k < n; k++) {
                    double v = A.getQuick(j, k);
                    double v1 = A.getQuick(j, i);
                    A.setQuick(j, k, v - v1 * A.getQuick(i, k));
                }
            }
        }
        return pi;
    }

    public static void LUPInvert(Matrix2D IA, Matrix2D LU, int[] P) {
        int N = LU.rows();
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N; i++) {
                double val = P[i] == j ? 1.0 : 0.0;
                IA.setQuick(i, j, val);

                for (int k = 0; k < i; k++) {
                    //IA[i][j] -= LU[i][k] * IA[k][j];
                    double v = IA.getQuick(i, j) - LU.getQuick(i, k) * IA.getQuick(k, j);
                    IA.setQuick(i, j, v);
                }
            }

            for (int i = N - 1; i >= 0; i--) {
                for (int k = i + 1; k < N; k++) {
                    // IA[i][j] -= LU[i][k] * IA[k][j];
                    double v = IA.getQuick(i, j) - LU.getQuick(i, k) * IA.getQuick(k, j);
                    IA.setQuick(i, j, v);
                }
                // IA[i][j] /= A[i][i];
                double v = IA.getQuick(i, j) / LU.getQuick(i, i);
                IA.setQuick(i, j, v);
            }
        }
    }
}
