/*
 * @test
 * @summary JLS 3.1: programs are written in Unicode; supplementary characters
 *          are represented in UTF-16 as surrogate pairs; the platform can
 *          convert between 16-bit code units and 32-bit code points.
 * @run main UnicodeCodePoints
 */

public class UnicodeCodePoints {
    public static void main(String[] args) {
        // A supplementary character U+1D700 occupies two UTF-16 code units.
        String omega = "𝜀";
        if (omega.length() != 2) {
            throw new AssertionError("supplementary char must be 2 code units");
        }
        if (omega.codePointCount(0, omega.length()) != 1) {
            throw new AssertionError("supplementary char must be 1 code point");
        }
        if (omega.codePointAt(0) != 0x1D700) {
            throw new AssertionError("wrong code point: " + Integer.toHexString(omega.codePointAt(0)));
        }

        // Code points U+0000..U+FFFF have the same value as their UTF-16 unit.
        char bmp = 'Ω';
        if (bmp != 0x03A9) throw new AssertionError("BMP char/value mismatch");

        // Code points above U+FFFF are converted to surrogate pairs.
        char[] pair = Character.toChars(0x1D700);
        if (pair.length != 2 || pair[0] != 0xD835 || pair[1] != 0xDF00) {
            throw new AssertionError("surrogate pair decomposition mismatch");
        }

        System.out.println("PASS");
    }
}
