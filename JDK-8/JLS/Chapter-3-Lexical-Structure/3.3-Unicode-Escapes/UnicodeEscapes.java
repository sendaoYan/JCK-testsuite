/*
 * @test
 * @summary JLS 3.3 (Java SE 8): a Unicode escape contributes one UTF-16 code
 *          unit and is translated before lexical analysis, so it may form part
 *          of an identifier or a character or string literal.
 * @run main UnicodeEscapes
 */

public class UnicodeEscapes {
    public static void main(String[] args) {
        // A Unicode escape may appear inside an identifier.
        int \u0041 = 42;
        if (\u0041 != 42) throw new AssertionError("unicode escape in identifier");

        // A Unicode escape may denote a character or appear inside a string.
        if ('\u005a' != 'Z') throw new AssertionError("unicode escape in char literal");
        if (!"\u005a".equals("Z")) throw new AssertionError("unicode escape in string");

        System.out.println("PASS");
    }
}
