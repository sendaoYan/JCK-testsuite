/*
 * @test
 * @summary JLS 4.2.1: the values of the integral types are integers in the
 *          specified ranges: byte -128..127, short -32768..32767, int
 *          -2147483648..2147483647, long -9223372036854775808..9223372036854775807,
 *          char 0..65535 (unsigned 16-bit).
 * @run main IntegralTypesAndValues
 */

public class IntegralTypesAndValues {
    public static void main(String[] args) {
        if (Byte.MIN_VALUE != -128) throw new AssertionError("Byte.MIN_VALUE");
        if (Byte.MAX_VALUE != 127) throw new AssertionError("Byte.MAX_VALUE");
        if (Short.MIN_VALUE != -32768) throw new AssertionError("Short.MIN_VALUE");
        if (Short.MAX_VALUE != 32767) throw new AssertionError("Short.MAX_VALUE");
        if (Integer.MIN_VALUE != -2147483648) throw new AssertionError("Integer.MIN_VALUE");
        if (Integer.MAX_VALUE != 2147483647) throw new AssertionError("Integer.MAX_VALUE");
        if (Long.MIN_VALUE != -9223372036854775808L) throw new AssertionError("Long.MIN_VALUE");
        if (Long.MAX_VALUE != 9223372036854775807L) throw new AssertionError("Long.MAX_VALUE");

        // char values are 16-bit unsigned integers: 0..65535.
        if (Character.MIN_VALUE != '\u0000') throw new AssertionError("Character.MIN_VALUE");
        if (Character.MAX_VALUE != '￿') throw new AssertionError("Character.MAX_VALUE");
        if ((int) Character.MAX_VALUE != 65535) throw new AssertionError("char max must be 65535");
        if ((int) Character.MIN_VALUE != 0) throw new AssertionError("char min must be 0");

        // byte/short/int/long are signed two's-complement.
        if (Byte.SIZE != 8 || Short.SIZE != 16 || Integer.SIZE != 32 || Long.SIZE != 64) {
            throw new AssertionError("integral type bit sizes");
        }
        if (Character.SIZE != 16) throw new AssertionError("char size must be 16");

        System.out.println("PASS");
    }
}
