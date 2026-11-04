/*
 * @test
 * @summary JLS 3.10.4 (Java SE 8): it is a compile-time error for a line
 *          terminator to appear between the quotes of a character literal;
 *          \u000a (LF) and \u000d (CR) are translated to raw line terminators.
 * @compile/fail CharacterLiteralLineTerminator.java
 */
public class CharacterLiteralLineTerminator {
    char a = '\u000a';
    char b = '\u000d';
}
