/*
 * @test
 * @summary JLS 3.10.1 (Java SE 8): the exact largest positive, most negative, and
 *          -1 int and long literals given in the specification have the specified
 *          values, in every radix.
 * @run main IntegerLiteralBoundaries
 */

public class IntegerLiteralBoundaries {
    public static void main(String[] args) {
        // ---- int: largest positive = 2^31 - 1 ----
        checkInt(2147483647, Integer.MAX_VALUE, "decimal int max");
        checkInt(0x7fff_ffff, Integer.MAX_VALUE, "hex int max");
        checkInt(0177_7777_7777, Integer.MAX_VALUE, "octal int max");
        checkInt(0b0111_1111_1111_1111_1111_1111_1111_1111, Integer.MAX_VALUE, "binary int max");

        // ---- int: most negative = -2^31 ----
        checkInt(-2147483648, Integer.MIN_VALUE, "decimal int min");
        checkInt(0x8000_0000, Integer.MIN_VALUE, "hex int min");
        checkInt(0200_0000_0000, Integer.MIN_VALUE, "octal int min");
        checkInt(0b1000_0000_0000_0000_0000_0000_0000_0000, Integer.MIN_VALUE, "binary int min");

        // ---- int: -1 ----
        checkInt(0xffff_ffff, -1, "hex int -1");
        checkInt(0377_7777_7777, -1, "octal int -1");
        checkInt(0b1111_1111_1111_1111_1111_1111_1111_1111, -1, "binary int -1");

        // ---- long: largest positive = 2^63 - 1 ----
        checkLong(9223372036854775807L, Long.MAX_VALUE, "decimal long max");
        checkLong(0x7fff_ffff_ffff_ffffL, Long.MAX_VALUE, "hex long max");
        checkLong(07_7777_7777_7777_7777_7777L, Long.MAX_VALUE, "octal long max");
        checkLong(0b0111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111L,
                Long.MAX_VALUE, "binary long max");

        // ---- long: most negative = -2^63 ----
        checkLong(-9223372036854775808L, Long.MIN_VALUE, "decimal long min");
        checkLong(0x8000_0000_0000_0000L, Long.MIN_VALUE, "hex long min");
        checkLong(010_0000_0000_0000_0000_0000L, Long.MIN_VALUE, "octal long min");
        checkLong(0b1000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000L,
                Long.MIN_VALUE, "binary long min");

        // ---- long: -1 ----
        checkLong(0xffff_ffff_ffff_ffffL, -1L, "hex long -1");
        checkLong(017_7777_7777_7777_7777_7777L, -1L, "octal long -1");
        checkLong(0b1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111L,
                -1L, "binary long -1");

        System.out.println("PASS");
    }

    static void checkInt(int actual, int expected, String what) {
        if (actual != expected) {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }

    static void checkLong(long actual, long expected, String what) {
        if (actual != expected) {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }
}
