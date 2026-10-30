/*
 * @test
 * @summary JVMS 6.5: the numeric type conversion instructions (i2b, i2s, l2i, d2i,
 *          f2i, i2l) truncate toward zero; NaN converts to 0 and infinities
 *          convert to the extreme representable values.
 * @compile TypeConv.jasm
 * @run main TypeConversionTest
 */

public class TypeConversionTest {
    public static void main(String[] args) {
        // i2b truncates to 8 bits (sign-extended)
        if (TypeConv.i2b(256) != 0) throw new AssertionError("i2b 256 -> " + TypeConv.i2b(256));
        if (TypeConv.i2b(255) != -1) throw new AssertionError("i2b 255 -> " + TypeConv.i2b(255));
        if (TypeConv.i2b(-1) != -1) throw new AssertionError("i2b -1");

        // i2s truncates to 16 bits (sign-extended)
        if (TypeConv.i2s(65536) != 0) throw new AssertionError("i2s 65536");
        if (TypeConv.i2s(65535) != -1) throw new AssertionError("i2s 65535");

        // d2i truncates toward zero
        if (TypeConv.d2i(3.99) != 3) throw new AssertionError("d2i 3.99");
        if (TypeConv.d2i(-3.99) != -3) throw new AssertionError("d2i -3.99");
        if (TypeConv.d2i(Double.NaN) != 0) throw new AssertionError("d2i NaN");
        if (TypeConv.d2i(Double.POSITIVE_INFINITY) != Integer.MAX_VALUE)
            throw new AssertionError("d2i +inf");
        if (TypeConv.d2i(Double.NEGATIVE_INFINITY) != Integer.MIN_VALUE)
            throw new AssertionError("d2i -inf");

        // f2i truncates toward zero
        if (TypeConv.f2i(3.99f) != 3) throw new AssertionError("f2i 3.99");
        if (TypeConv.f2i(Float.NaN) != 0) throw new AssertionError("f2i NaN");

        // l2i keeps the low 32 bits
        if (TypeConv.l2i(0x1_0000_0001L) != 1) throw new AssertionError("l2i low 32 bits");

        // i2l sign-extends
        if (TypeConv.i2l(-1) != -1L) throw new AssertionError("i2l -1");

        System.out.println("PASS");
    }
}
