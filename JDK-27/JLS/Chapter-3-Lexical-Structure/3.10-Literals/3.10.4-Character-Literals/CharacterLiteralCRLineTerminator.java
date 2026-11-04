/*
 * @test
 * @summary JLS 3.10.4: a line terminator may not appear between the quotes of a
 *          character literal; the Unicode escape \u000d is translated to a raw
 *          carriage return, so it is a compile-time error here.
 * @compile/fail CharacterLiteralCRLineTerminator.java
 */
public class CharacterLiteralCRLineTerminator {
    char c = '\u000d';
}
