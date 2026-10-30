/*
 * @test
 * @summary JLS 4.2 (Java SE 8): integral types have two's-complement ranges,
 *          char is an unsigned 16-bit type, and floating-point types have
 *          signed zeros, infinities, and NaN.
 * @run main PrimitiveValues
 */

public class PrimitiveValues {
    public static void main(String[] args) {
        if (Byte.MIN_VALUE != -128 || Byte.MAX_VALUE != 127) throw new AssertionError("byte range");
        if (Short.MIN_VALUE != -32768 || Short.MAX_VALUE != 32767) throw new AssertionError("short range");
        if (Integer.MIN_VALUE != 0x80000000 || Integer.MAX_VALUE != 0x7fffffff) throw new AssertionError("int range");
        if (Long.MIN_VALUE != 0x8000000000000000L || Long.MAX_VALUE != 0x7fffffffffffffffL) throw new AssertionError("long range");

        if ((int) Character.MIN_VALUE != 0 || (int) Character.MAX_VALUE != 65535)
            throw new AssertionError("char is an unsigned 16-bit type");

        if (Float.POSITIVE_INFINITY != Float.POSITIVE_INFINITY) throw new AssertionError("+inf");
        if (Float.NEGATIVE_INFINITY == Float.POSITIVE_INFINITY) throw new AssertionError("+inf != -inf");
        if (!Float.isNaN(Float.NaN)) throw new AssertionError("NaN");
        if (Float.NaN == Float.NaN) throw new AssertionError("NaN != NaN");

        float pz = 0.0f, nz = -0.0f;
        if (pz != nz) throw new AssertionError("0.0f == -0.0f");
        if (1.0f / pz != Float.POSITIVE_INFINITY) throw new AssertionError("1/0.0f = +inf");
        if (1.0f / nz != Float.NEGATIVE_INFINITY) throw new AssertionError("1/-0.0f = -inf");

        double pzd = 0.0, nzd = -0.0;
        if (pzd != nzd) throw new AssertionError("0.0 == -0.0");
        if (Double.POSITIVE_INFINITY <= Double.MAX_VALUE) throw new AssertionError("+inf > Double.MAX_VALUE");
        if (!Double.isNaN(0.0 / 0.0)) throw new AssertionError("0.0/0.0 is NaN");

        System.out.println("PASS");
    }
}
