/*
 * @test
 * @summary JLS 3.10.1 (Java SE 8): integer literals may be decimal, hexadecimal,
 *          octal, or binary (0b), may contain underscores, and take an optional
 *          long suffix L/l.
 * @run main IntegerLiterals
 */

public class IntegerLiterals {
    public static void main(String[] args) {
        if (0 != 0 || 42 != 42) throw new AssertionError("decimal");
        if (0x10 != 16 || 0XFF != 255) throw new AssertionError("hex");
        if (010 != 8) throw new AssertionError("octal");
        if (0b1010 != 10 || 0B11 != 3) throw new AssertionError("binary");

        if (1_000_000 != 1000000) throw new AssertionError("underscore decimal");
        if (0xCA_FE != 0xCAFE) throw new AssertionError("underscore hex");
        if (0b1010_0101 != 0xA5) throw new AssertionError("underscore binary");

        if (42L != 42L || 0L != 0L) throw new AssertionError("long suffix");
        if (0xFFFFFFFFFFFFFFFFL != -1L) throw new AssertionError("long hex wrap");

        if (2147483647 != Integer.MAX_VALUE) throw new AssertionError("int max");
        if (-2147483648 != Integer.MIN_VALUE) throw new AssertionError("int min");
        if (9223372036854775807L != Long.MAX_VALUE) throw new AssertionError("long max");

        System.out.println("PASS");
    }
}
