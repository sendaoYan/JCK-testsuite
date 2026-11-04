/*
 * @test
 * @summary JLS 3.10.5: a line terminator may not appear in a string literal
 *          except as an escape sequence; the Unicode escape \u000d is
 *          translated to a raw carriage return, so the string is unclosed.
 * @compile/fail StringLiteralCRLineTerminator.java
 */
public class StringLiteralCRLineTerminator {
    public static void main(String[] args) {
        String s = "x\u000d";
    }
}
