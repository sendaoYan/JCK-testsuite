/*
 * @test
 * @summary JLS 3.3: a Unicode escape may use one or more u characters after the
 *          backslash (\uuuu0041 is the same as A); a Unicode escape may
 *          appear wherever an input character may appear, including inside an
 *          identifier and inside a string literal.
 * @run main MultipleUUnicodeEscape
 */

public class MultipleUUnicodeEscape {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    // \uuuu0041 is translated to 'A', so this declares a field named A.
    static int \uuuu0041 = 42;

    public static void main(String[] args) {
        check(A == 42, "\\uuuu0041 must denote field A");

        // One or more u characters are consumed by the UnicodeMarker.
        char c = '\uuu0041';
        check(c == 'A', "\\uuu0041 in a char literal");

        String s = "\uuuu0061b";
        check(s.equals("ab"), "\\uuuu0061 in a string literal");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
