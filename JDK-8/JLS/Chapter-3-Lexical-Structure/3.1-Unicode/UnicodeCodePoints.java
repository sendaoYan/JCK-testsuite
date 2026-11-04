/*
 * @test
 * @summary JLS 3.1 (Java SE 8): programs are written in Unicode; supplementary
 *          characters are represented in UTF-16 as surrogate pairs; code
 *          points above U+FFFF are converted to surrogate pairs.
 * @run main UnicodeCodePoints
 */

public class UnicodeCodePoints {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        // A supplementary character U+1D700 occupies two UTF-16 code units.
        String omega = "𝜀";
        check(omega.length() == 2, "supplementary char is 2 code units");
        check(omega.codePointCount(0, omega.length()) == 1, "supplementary char is 1 code point");
        check(omega.codePointAt(0) == 0x1D700, "supplementary code point value");

        // Code points U+0000..U+FFFF have the same value as their UTF-16 unit.
        char bmp = 'Ω';
        check(bmp == 0x03A9, "BMP char/value");

        // Code points above U+FFFF are converted to surrogate pairs.
        char[] pair = Character.toChars(0x1D700);
        check(pair.length == 2 && pair[0] == 0xD835 && pair[1] == 0xDF00,
                "surrogate pair decomposition");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
