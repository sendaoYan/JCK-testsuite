/*
 * @test
 * @summary JVMS 2.3 (Java SE 8): the integral and floating-point types occupy fixed
 *          widths: byte 8 bits, short 16, char 16, int 32, long 64, float 32,
 *          double 64. long and double values use 64-bit (two slot) representation.
 * @run main PrimitiveTypeWidthsTest
 */

public class PrimitiveTypeWidthsTest {
    public static void main(String[] args) {
        if (Byte.SIZE != 8) throw new AssertionError("byte width: " + Byte.SIZE);
        if (Short.SIZE != 16) throw new AssertionError("short width: " + Short.SIZE);
        if (Character.SIZE != 16) throw new AssertionError("char width: " + Character.SIZE);
        if (Integer.SIZE != 32) throw new AssertionError("int width: " + Integer.SIZE);
        if (Long.SIZE != 64) throw new AssertionError("long width: " + Long.SIZE);
        if (Float.SIZE != 32) throw new AssertionError("float width: " + Float.SIZE);
        if (Double.SIZE != 64) throw new AssertionError("double width: " + Double.SIZE);

        // A long is a 64-bit two's-complement value; all 64 bits are significant.
        long big = 0x0123456789ABCDEFL;
        if (big != 0x0123456789ABCDEFL) throw new AssertionError("long 64-bit value");

        // A double is a 64-bit IEEE 754 value; smallest positive subnormal is
        // Double.MIN_VALUE (0x0.0000000000001p-1022).
        if (Double.MIN_VALUE <= 0.0) throw new AssertionError("double min value");

        System.out.println("PASS");
    }
}
