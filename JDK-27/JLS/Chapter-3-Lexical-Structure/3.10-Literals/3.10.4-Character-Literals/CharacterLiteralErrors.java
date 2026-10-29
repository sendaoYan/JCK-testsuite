/*
 * @test
 * @summary JLS 3.10.4: a character literal must contain exactly one character
 *          (a SingleCharacter or an EscapeSequence); the empty literal and a
 *          two-character literal are compile-time errors.
 * @compile/fail CharacterLiteralErrors.java
 */

public class CharacterLiteralErrors {
    char empty = '';
    char twoChars = 'ab';
}
