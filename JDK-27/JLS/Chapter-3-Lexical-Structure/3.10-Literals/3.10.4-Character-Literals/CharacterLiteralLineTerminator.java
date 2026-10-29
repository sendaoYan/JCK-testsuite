/*
 * @test
 * @summary JLS 3.10.4: it is a compile-time error for a line terminator to
 *          appear between the opening and closing ' of a character literal.
 *          \u000a is translated to a linefeed in step 1 (JLS 3.3), so
 *          '\u000a' is invalid.
 * @compile/fail CharacterLiteralLineTerminator.java
 */
public class CharacterLiteralLineTerminator {
    char c = '\u000a';
}
