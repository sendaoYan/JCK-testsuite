/*
 * @test
 * @summary JLS 3.10.1: integer literals may be decimal, hexadecimal, octal, or
 *          binary; an L/l suffix makes the literal type long, otherwise int;
 *          underscores may separate digits.
 * @run main IntegerLiteralValues
 */

public class IntegerLiteralValues {
    public static void main(String[] args) {
        // Decimal
        if (0 != 0) throw new AssertionError("decimal 0");
        if (1996 != 1996) throw new AssertionError("decimal 1996");

        // Hexadecimal (0x / 0X, upper- or lower-case digits)
        if (0xDada_Cafe != 0xDADACAFE) throw new AssertionError("hex literal");
        if (0x00_FF__00_FF != 0x00FF00FF) throw new AssertionError("hex underscores");

        // Octal
        if (0372 != 250) throw new AssertionError("octal 0372 != 250");
        if (00 != 0) throw new AssertionError("octal 00 != 0");

        // Binary (0b / 0B)
        if (0b1010 != 10) throw new AssertionError("binary 0b1010 != 10");
        if (0B1111 != 15) throw new AssertionError("binary 0B1111 != 15");

        // Long suffix L/l: literal is of type long
        if (0l != 0L) throw new AssertionError("0l != 0L");
        if (0777L != 511L) throw new AssertionError("0777L != 511L");
        if (0x100000000L != 4294967296L) throw new AssertionError("0x100000000L");
        if (2_147_483_648L != 2147483648L) throw new AssertionError("2_147_483_648L");
        if (0xC0B0L != 0xC0B0L) throw new AssertionError("0xC0B0L");

        // Underscores between digits in decimal and octal
        if (1_000 != 1000) throw new AssertionError("1_000 != 1000");
        if (0_1 != 1) throw new AssertionError("0_1 != 1 (underscore after leading 0)");
        if (1_2_3 != 123) throw new AssertionError("1_2_3 != 123");

        // int literal values are of type int; check representative boundaries
        if (Integer.MAX_VALUE != 2147483647) throw new AssertionError("int max");

        System.out.println("PASS");
    }
}
