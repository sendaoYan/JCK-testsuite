/*
 * @test
 * @summary JLS 3.3 + 3.10.5: a Unicode escape \u000a is translated to a
 *          real linefeed in step 1, which becomes a LineTerminator in step
 *          2 (JLS 3.4), so the string literal is unclosed. Compile error.
 * @compile/fail UnicodeEscapeLineTerminator.java
 */
public class UnicodeEscapeLineTerminator {
    public static void main(String[] args) {
        String s = "\u000a";
    }
}
