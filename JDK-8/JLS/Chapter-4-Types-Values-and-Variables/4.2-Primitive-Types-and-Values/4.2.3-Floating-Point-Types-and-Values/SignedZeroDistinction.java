/*
 * @test
 * @summary JLS 4.2.3 (Java SE 8): positive zero and negative zero compare equal
 *          (0.0 == -0.0 is true, 0.0 > -0.0 is false) but other operations can
 *          distinguish them: 1.0/0.0 is positive infinity while 1.0/-0.0 is
 *          negative infinity, and the raw bit patterns differ.
 * @run main SignedZeroDistinction
 */

public class SignedZeroDistinction {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        double pz = 0.0, nz = -0.0;
        float fpz = 0.0f, fnz = -0.0f;

        // Positive and negative zero compare equal.
        check(pz == nz, "0.0 == -0.0");
        check(!(pz > nz), "0.0 > -0.0 is false");
        check(!(pz < nz), "0.0 < -0.0 is false");
        check(fpz == fnz, "0.0f == -0.0f");

        // Division distinguishes the two zeros by sign of the result.
        check(1.0 / pz == Double.POSITIVE_INFINITY, "1.0/0.0 is +infinity");
        check(1.0 / nz == Double.NEGATIVE_INFINITY, "1.0/-0.0 is -infinity");
        check(1.0f / fpz == Float.POSITIVE_INFINITY, "1.0f/0.0f is +infinity");
        check(1.0f / fnz == Float.NEGATIVE_INFINITY, "1.0f/-0.0f is -infinity");

        // The raw bit patterns differ (IEEE 754 sign bit).
        check(Double.doubleToRawLongBits(pz) != Double.doubleToRawLongBits(nz),
              "raw double bits of +0.0 and -0.0 differ");
        check(Float.floatToRawIntBits(fpz) != Float.floatToRawIntBits(fnz),
              "raw float bits of +0.0f and -0.0f differ");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
