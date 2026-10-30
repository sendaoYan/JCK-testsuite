/*
 * @test
 * @summary JLS 15.4: floating-point expressions denote IEEE 754 binary32/binary64
 *          values, including NaN, signed infinity and signed zero.
 * @run main FloatingPointSemantics
 */

public class FloatingPointSemantics {
    public static void main(String[] args) {
        double nan = Double.NaN;
        if (nan == nan) throw new AssertionError("NaN must not equal itself");
        if (!Double.isNaN(nan)) throw new AssertionError("NaN");

        double inf = 1.0 / 0.0;
        if (inf != Double.POSITIVE_INFINITY) throw new AssertionError("+inf: " + inf);
        double ninf = -1.0 / 0.0;
        if (ninf != Double.NEGATIVE_INFINITY) throw new AssertionError("-inf: " + ninf);

        double nz = -0.0;
        if (nz != 0.0) throw new AssertionError("-0.0 == 0.0");
        if (Double.doubleToRawLongBits(nz) != 0x8000000000000000L)
            throw new AssertionError("sign bit of -0.0");

        float f = 1.0f / 0.0f;
        if (f != Float.POSITIVE_INFINITY) throw new AssertionError("float +inf: " + f);

        System.out.println("PASS");
    }
}
