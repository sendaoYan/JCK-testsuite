/*
 * @test
 * @summary JLS 3.3 + 3.4 + 3.10.5: the Unicode escape \u000d is translated to a
 *          raw carriage return in the first lexical translation step, which is
 *          a line terminator, so a string literal containing it is unclosed.
 * @compile/fail UnicodeEscapeCRLineTerminator.java
 */
public class UnicodeEscapeCRLineTerminator {
    public static void main(String[] args) {
        String s = "x\u000d";
    }
}
