/*
 * @test
 * @summary JLS 15.17/15.18/15.4: floating-point arithmetic at the IEEE 754
 *          boundaries.  NaN propagates through operations, division by zero
 *          yields a signed infinity, 0.0/0.0 and 0.0*Inf yield NaN, and -0.0 is
 *          numerically equal to 0.0 yet has a distinct sign (1.0/-0.0 is -Inf).
 * @run main FloatDoubleBoundaries
 */

public class FloatDoubleBoundaries {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static double div(double a, double b) { return a / b; }
    static double mul(double a, double b) { return a * b; }

    public static void main(String[] args) {
        double nan = Double.NaN;
        check(Double.isNaN(nan + 1.0), "NaN + 1 == NaN");
        check(Double.isNaN(nan * 0.0), "NaN * 0 == NaN");
        check(Double.isNaN(nan - nan), "NaN - NaN == NaN");
        check(Double.isNaN(0.0 * Double.POSITIVE_INFINITY), "0 * Inf == NaN");
        check(Double.isNaN(Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY), "Inf - Inf == NaN");

        check(div(1.0, 0.0) == Double.POSITIVE_INFINITY, "1.0/0.0 == +Inf");
        check(div(-1.0, 0.0) == Double.NEGATIVE_INFINITY, "-1.0/0.0 == -Inf");
        check(Double.isNaN(div(0.0, 0.0)), "0.0/0.0 == NaN");

        double posZero = 0.0, negZero = -0.0;
        check(posZero == negZero, "0.0 == -0.0");
        check(Double.doubleToLongBits(posZero) != Double.doubleToLongBits(negZero), "-0.0 has distinct bits");
        check(div(1.0, negZero) == Double.NEGATIVE_INFINITY, "1.0/-0.0 == -Inf");
        check(div(1.0, posZero) == Double.POSITIVE_INFINITY, "1.0/0.0 == +Inf");

        check(mul(Double.MAX_VALUE, 2.0) == Double.POSITIVE_INFINITY, "MAX_VALUE*2 == +Inf");
        check(mul(-Double.MAX_VALUE, 2.0) == Double.NEGATIVE_INFINITY, "-MAX_VALUE*2 == -Inf");

        float fnan = Float.NaN;
        check(Float.isNaN(fnan), "float NaN");
        check(1.0f / 0.0f == Float.POSITIVE_INFINITY, "1.0f/0.0f == +Inf");
        check(1.0f / -0.0f == Float.NEGATIVE_INFINITY, "1.0f/-0.0f == -Inf");
        check(-0.0f == 0.0f, "float -0.0 == 0.0");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
