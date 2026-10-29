/*
 * @test
 * @summary JLS 3.3: A Unicode escape \\uxxxx is translated to the UTF-16 code
 *          unit xxxx; eligible backslash rules; escaped chars do not re-escape.
 * @run main UnicodeEscapeTranslation
 */

public class UnicodeEscapeTranslation {
    // \u0041 is translated to the letter A before parsing; this declares a field A.
    static int \u0041 = 42;
    // \u0062 is 'b'; declare a second field using a Unicode escape.
    static int \u0062 = 7;

    public static void main(String[] args) {
        // (1) Basic translation: \u0041 -> A, \u0062 -> b.
        if (A != 42) {
            throw new AssertionError("\\u0041 did not denote 'A' field");
        }
        if (b != 7) {
            throw new AssertionError("\\u0062 did not denote 'b' field");
        }

        // (2) "\\u2122=\u2122": the first two backslashes are preserved as raw
        // input (the second is ineligible by the contiguous-backslash rule),
        // while the third backslash begins the escape \u2122 -> TM.
        String s = "\\u2122=\u2122";
        String expected = "\\u2122=" + "\u2122";
        if (!s.equals(expected)) {
            throw new AssertionError("backslash eligibility mismatch: " + s);
        }

        // (3) A supplementary character is represented by two consecutive escapes.
        // U+1D700 (MATHEMATICAL ITALIC SMALL OMEGA) is the surrogate pair
        // \uD835\uDF00.
        String omega = "\uD835\uDF00";
        int cp = omega.codePointAt(0);
        if (cp != 0x1D700) {
            throw new AssertionError("supplementary escape pair mismatch: " + Integer.toHexString(cp));
        }
        if (omega.length() != 2) {
            throw new AssertionError("supplementary escape should occupy 2 code units");
        }

        System.out.println("PASS");
    }
}
