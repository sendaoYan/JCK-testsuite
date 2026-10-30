/*
 * @test
 * @summary JLS 4.2.3 (Java SE 8): IEEE 754 special values.  NaN is unordered and
 *          distinct from itself, signed zero is distinguishable by bit pattern
 *          and by 1/x, division by zero yields signed infinity, subnormals
 *          underflow to zero, and rounding makes 0.1 + 0.2 differ from 0.3.
 * @run main FloatSpecialValueTest
 */

public class FloatSpecialValueTest {
    static double ddiv(double a, double b) { return a / b; }

    public static void main(String[] args) {
        // ---- NaN ----
        double nan = ddiv(0.0, 0.0);
        if (nan == nan) throw new AssertionError("NaN must not equal itself");
        if (!Double.isNaN(nan)) throw new AssertionError("0.0/0.0 is NaN");
        if (Double.doubleToLongBits(Double.NaN) != 0x7ff8000000000000L)
            throw new AssertionError("canonical NaN bit pattern");

        // ---- signed infinities ----
        if (ddiv(1.0, 0.0) != Double.POSITIVE_INFINITY) throw new AssertionError("+Inf");
        if (ddiv(-1.0, 0.0) != Double.NEGATIVE_INFINITY) throw new AssertionError("-Inf");
        if (ddiv(1.0, -0.0) != Double.NEGATIVE_INFINITY) throw new AssertionError("1/-0 = -Inf");
        if (0.0 * Double.POSITIVE_INFINITY == 0.0 * Double.POSITIVE_INFINITY)
            throw new AssertionError("0*Inf is NaN");
        if (Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY == 0.0)
            throw new AssertionError("Inf-Inf is NaN");

        // ---- signed zero ----
        if (Double.doubleToLongBits(0.0) != 0x0000000000000000L)
            throw new AssertionError("+0.0 bits");
        if (Double.doubleToLongBits(-0.0) != 0x8000000000000000L)
            throw new AssertionError("-0.0 bits");
        if (Double.compare(-0.0, 0.0) >= 0) throw new AssertionError("-0.0 < +0.0");

        // ---- bit patterns of 1.0 and infinities ----
        if (Double.doubleToRawLongBits(1.0) != 0x3ff0000000000000L)
            throw new AssertionError("1.0 bits");
        if (Double.doubleToRawLongBits(Double.POSITIVE_INFINITY) != 0x7ff0000000000000L)
            throw new AssertionError("+Inf bits");
        if (Double.doubleToRawLongBits(Double.NEGATIVE_INFINITY) != 0xfff0000000000000L)
            throw new AssertionError("-Inf bits");

        // ---- 32-bit float analogs ----
        if (Float.floatToRawIntBits(1.0f) != 0x3f800000) throw new AssertionError("1.0f bits");
        if (Float.floatToIntBits(Float.POSITIVE_INFINITY) != 0x7f800000) throw new AssertionError("+Inf float");
        if (Float.floatToIntBits(Float.NaN) != 0x7fc00000) throw new AssertionError("NaN float");

        // ---- subnormals and underflow ----
        if (Double.MIN_VALUE == 0.0) throw new AssertionError("MIN_VALUE is subnormal, not zero");
        if (Double.MIN_VALUE / 2 != 0.0) throw new AssertionError("subnormal underflows to zero");
        if (Double.MIN_NORMAL != 0x1.0p-1022) throw new AssertionError("MIN_NORMAL");
        if (Double.MAX_VALUE * 2 != Double.POSITIVE_INFINITY)
            throw new AssertionError("overflow to +Inf");

        // ---- rounding ----
        if (0.1 + 0.2 == 0.3) throw new AssertionError("0.1+0.2 rounds to != 0.3");

        // ---- Math.sqrt special cases ----
        if (!Double.isNaN(Math.sqrt(-1.0))) throw new AssertionError("sqrt(-1) is NaN");
        if (Math.sqrt(0.0) != 0.0) throw new AssertionError("sqrt(0) = +0");
        if (Double.doubleToLongBits(Math.sqrt(-0.0)) != Double.doubleToLongBits(-0.0))
            throw new AssertionError("sqrt(-0) = -0");

        // ---- Double.compare total order ----
        if (Double.compare(Double.NaN, Double.POSITIVE_INFINITY) <= 0)
            throw new AssertionError("NaN > +Inf in compare");
        if (Double.compare(Double.NaN, Double.NaN) != 0) throw new AssertionError("NaN==NaN in compare");

        System.out.println("PASS");
    }
}
