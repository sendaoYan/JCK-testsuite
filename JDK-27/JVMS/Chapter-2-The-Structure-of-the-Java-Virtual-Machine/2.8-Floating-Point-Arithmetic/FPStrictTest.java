/*
 * @test
 * @summary JVMS 2.8: floating-point arithmetic is strict — strictfp and
 *          non-strictfp evaluation produce bit-identical results, so no
 *          extended-exponent intermediate ever leaks past a value set
 *          conversion on this VM (JEP 306 always-strict semantics).
 * @run main FPStrictTest
 */

public class FPStrictTest {
    static strictfp double strict(double a, double b) {
        double x = a * b;
        double y = a / b;
        double z = a + b;
        double w = a - b;
        return (x + y) * (z - w) + Math.sqrt(Math.abs(a)) - Math.cbrt(Math.abs(b));
    }
    static double plain(double a, double b) {
        double x = a * b;
        double y = a / b;
        double z = a + b;
        double w = a - b;
        return (x + y) * (z - w) + Math.sqrt(Math.abs(a)) - Math.cbrt(Math.abs(b));
    }

    public static void main(String[] args) {
        double[] xs = {
            1.0, 2.0, 3.0, Math.PI, Math.E,
            Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL,
            0.1, 1e300, 1e-300, 7.5, -1.0
        };
        for (double a : xs) {
            for (double b : xs) {
                double s = strict(a, b);
                double p = plain(a, b);
                if (Double.doubleToLongBits(s) != Double.doubleToLongBits(p)) {
                    throw new AssertionError("strictfp differs from non-strictfp:"
                        + " a=" + a + " b=" + b
                        + " strict=" + Long.toHexString(Double.doubleToLongBits(s))
                        + " plain=" + Long.toHexString(Double.doubleToLongBits(p)));
                }
            }
        }

        // Value set conversion still distinguishes storage widths: a float
        // value is rounded to the float value set, a double to the double set.
        if ((double) 0.1f == 0.1d) {
            throw new AssertionError("float 0.1 and double 0.1 must be distinct real values");
        }

        System.out.println("PASS");
    }
}
