/*
 * @test
 * @summary JVMS 6.3: arithmetic on the JVM.  Integer and long arithmetic are
 *          two's-complement and wrap on overflow; floating-point arithmetic
 *          follows IEEE 754, producing Infinity and NaN and distinguishing
 *          positive and negative zero.
 * @run main JvmArithmetic
 */

public class JvmArithmetic {
    public static void main(String[] args) {
        // Integer addition and multiplication wrap modulo 2^32.
        if (Integer.MAX_VALUE + 1 != Integer.MIN_VALUE)
            throw new AssertionError("int addition did not wrap");
        if (Integer.MIN_VALUE - 1 != Integer.MAX_VALUE)
            throw new AssertionError("int subtraction did not wrap");
        if (Integer.MAX_VALUE * 2 != -2)
            throw new AssertionError("int multiplication did not wrap");

        // Long addition and multiplication wrap modulo 2^64.
        if (Long.MAX_VALUE + 1 != Long.MIN_VALUE)
            throw new AssertionError("long addition did not wrap");
        if (Long.MAX_VALUE * 2 != -2L)
            throw new AssertionError("long multiplication did not wrap");

        // Integer division truncates toward zero; division by zero throws.
        if (-7 / 2 != -3) throw new AssertionError("int division truncation");
        if (7 / 2 != 3) throw new AssertionError("int division truncation");
        try {
            int x = 1 / 0;
            throw new AssertionError("int division by zero did not throw");
        } catch (ArithmeticException e) { }
        try {
            long x = 1L % 0L;
            throw new AssertionError("long remainder by zero did not throw");
        } catch (ArithmeticException e) { }

        // Floating-point division by zero yields signed Infinity, 0.0/0.0 is NaN.
        if (1.0 / 0.0 != Double.POSITIVE_INFINITY) throw new AssertionError("+inf");
        if (-1.0 / 0.0 != Double.NEGATIVE_INFINITY) throw new AssertionError("-inf");
        if (!Double.isNaN(0.0 / 0.0)) throw new AssertionError("0.0/0.0 not NaN");
        if (!Double.isNaN(Math.sqrt(-1.0))) throw new AssertionError("sqrt(-1) not NaN");

        // Positive and negative zero: == but distinguishable by 1/x.
        double pz = 0.0, nz = -0.0;
        if (pz != nz) throw new AssertionError("+0.0 != -0.0");
        if (1.0 / nz != Double.NEGATIVE_INFINITY) throw new AssertionError("-0.0 sign");

        System.out.println("PASS");
    }
}
