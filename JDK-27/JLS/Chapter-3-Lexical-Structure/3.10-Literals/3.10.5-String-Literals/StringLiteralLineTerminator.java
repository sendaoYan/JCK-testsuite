/*
 * @test
 * @summary JLS 3.10.5: it is a compile-time error for a line terminator to
 *          appear between the opening and closing " of a string literal.
 *          \u000a is translated to a linefeed in step 1 (JLS 3.3).
 * @compile/fail StringLiteralLineTerminator.java
 */
public class StringLiteralLineTerminator {
    String s = "\u000a";
}
