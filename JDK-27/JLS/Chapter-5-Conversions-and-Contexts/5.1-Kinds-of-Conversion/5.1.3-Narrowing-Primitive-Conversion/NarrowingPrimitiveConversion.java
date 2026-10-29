/*
 * @test
 * @summary JLS 5.1.3: the 22 narrowing primitive conversions. Narrowing an
 *          integer discards all but the n lowest order bits. Narrowing a
 *          floating-point number to an integral type: NaN -> 0, infinity ->
 *          min/max, otherwise round toward zero. double -> float rounds to
 *          nearest, may underflow to zero or overflow to infinity.
 * @run main NarrowingPrimitiveConversion
 */

public class NarrowingPrimitiveConversion {
    public static void main(String[] args) {
        // (short)0x12345678 == 0x5678 (low 16 bits).
        if ((short) 0x12345678 != 0x5678) throw new AssertionError("int->short low bits");

        // (byte)255 == -1 (low 8 bits, sign change).
        if ((byte) 255 != -1) throw new AssertionError("int->byte sign change");

        // int->char keeps low 16 bits.
        if ((char) 0x12345678 != 0x5678) throw new AssertionError("int->char low bits");

        // (int)1e20f == 2147483647 (too large -> largest int).
        if ((int) 1e20f != Integer.MAX_VALUE) throw new AssertionError("float->int overflow");

        // (int)NaN == 0.
        if ((int) Float.NaN != 0) throw new AssertionError("float NaN -> int 0");

        // Round toward zero.
        if ((int) 12345.6 != 12345) throw new AssertionError("double->int toward zero");
        if ((int) -12345.6 != -12345) throw new AssertionError("double->int toward zero (negative)");

        // (float)-1e100 == -Infinity (double -> float overflow).
        if ((float) -1e100 != Float.NEGATIVE_INFINITY) throw new AssertionError("double->float overflow");

        // (float)1e-50 == 0.0 (double -> float underflow to zero).
        if ((float) 1e-50 != 0.0f) throw new AssertionError("double->float underflow");

        // Narrowing a float to long: -infinity -> Long.MIN_VALUE, +infinity -> Long.MAX_VALUE.
        if ((long) Float.NEGATIVE_INFINITY != Long.MIN_VALUE) throw new AssertionError("float -inf -> long min");
        if ((long) Float.POSITIVE_INFINITY != Long.MAX_VALUE) throw new AssertionError("float +inf -> long max");

        // char to byte/short discards low bits (may become negative).
        if ((byte) (char) 0xff80 != (byte) 0x80) throw new AssertionError("char->byte");

        System.out.println("PASS");
    }
}
