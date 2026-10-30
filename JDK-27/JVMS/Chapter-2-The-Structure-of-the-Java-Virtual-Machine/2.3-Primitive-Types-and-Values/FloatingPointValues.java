/*
 * @test
 * @summary JVMS 2.3.2: float and double correspond to IEEE 754 binary32/binary64,
 *          with distinct positive/negative zeros, infinities, and NaNs that can be
 *          created and inspected via intBitsToFloat/longBitsToDouble.
 * @run main FloatingPointValues
 */

public class FloatingPointValues {
    public static void main(String[] args) {
        // float: +0.0 and -0.0 compare equal but have distinct bit patterns
        float pz = 0.0f, nz = -0.0f;
        if (pz != nz) throw new AssertionError("+0.0 == -0.0");
        if (Float.floatToRawIntBits(pz) != 0x00000000) throw new AssertionError("+0.0 bits");
        if (Float.floatToRawIntBits(nz) != 0x80000000) throw new AssertionError("-0.0 bits");
        if (1.0f / pz != Float.POSITIVE_INFINITY) throw new AssertionError("1.0f/+0.0f");
        if (1.0f / nz != Float.NEGATIVE_INFINITY) throw new AssertionError("1.0f/-0.0f");

        // NaN: unordered, and custom bit patterns round-trip exactly
        float nan = Float.NaN;
        if (nan == nan) throw new AssertionError("NaN == NaN");
        if (!Float.isNaN(nan)) throw new AssertionError("Float.NaN");
        float custom = Float.intBitsToFloat(0x7fc00001);
        if (!Float.isNaN(custom)) throw new AssertionError("custom NaN");
        if (Float.floatToRawIntBits(custom) != 0x7fc00001) throw new AssertionError("NaN bits");

        // double: +0.0 and -0.0
        double dpz = 0.0, dnz = -0.0;
        if (Double.doubleToRawLongBits(dpz) != 0L) throw new AssertionError("+0.0 double bits");
        if (Double.doubleToRawLongBits(dnz) != Long.MIN_VALUE) throw new AssertionError("-0.0 double bits");
        if (1.0 / dpz != Double.POSITIVE_INFINITY) throw new AssertionError("1.0/+0.0");
        if (1.0 / dnz != Double.NEGATIVE_INFINITY) throw new AssertionError("1.0/-0.0");

        double dnan = Double.NaN;
        if (dnan == dnan) throw new AssertionError("double NaN == NaN");
        if (!Double.isNaN(dnan)) throw new AssertionError("Double.NaN");

        System.out.println("PASS");
    }
}
