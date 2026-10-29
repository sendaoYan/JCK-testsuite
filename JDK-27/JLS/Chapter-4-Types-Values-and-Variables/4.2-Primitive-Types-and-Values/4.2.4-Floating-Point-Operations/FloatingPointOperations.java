/*
 * @test
 * @summary JLS 4.2.4: if either operand of a numerical operator is double the
 *          operation uses 64-bit arithmetic (result double); otherwise if one is
 *          float it uses 32-bit arithmetic (result float). Floating-point
 *          arithmetic follows IEEE 754 including overflow (to infinity) and
 *          gradual underflow; casts to integral types round toward zero.
 * @run main FloatingPointOperations
 */

public class FloatingPointOperations {
    public static void main(String[] args) {
        // Overflow produces positive infinity (Example 4.2.4-1).
        double d = 1e308;
        if (d * 10 != Double.POSITIVE_INFINITY) {
            throw new AssertionError("1e308*10 must overflow to +infinity, got " + (d * 10));
        }

        // 0.0/0.0 is NaN.
        d = 0.0 / 0.0;
        if (!Double.isNaN(d)) throw new AssertionError("0.0/0.0 must be NaN");

        // Cast to int rounds toward zero (not nearest).
        d = 12345.6;
        if ((int) d != 12345) throw new AssertionError("(int)12345.6 must be 12345");
        if ((int) (-d) != -12345) throw new AssertionError("(int)-12345.6 must be -12345");

        // If either operand is double the result is double (64-bit arithmetic).
        if (1.0 + 1 != 2.0) throw new AssertionError("double+int must yield double");
        Object r = 1.0 + 1;
        if (!(r instanceof Double)) throw new AssertionError("1.0+1 must be of type double");

        // Otherwise, a float operand yields a float result (32-bit arithmetic).
        Object f = 1.0f + 1;
        if (!(f instanceof Float)) throw new AssertionError("1.0f+1 must be of type float");

        // Gradual underflow: dividing a subnormal toward zero eventually reaches 0.0.
        double u = 1e-305;
        for (int i = 0; i < 6; i++) u /= 1e5;
        if (u != 0.0) throw new AssertionError("gradual underflow must reach 0.0");

        // Increment/decrement operators apply to floating-point values.
        double inc = 1.5;
        inc++;
        if (inc != 2.5) throw new AssertionError("double postfix ++");

        System.out.println("PASS");
    }
}
