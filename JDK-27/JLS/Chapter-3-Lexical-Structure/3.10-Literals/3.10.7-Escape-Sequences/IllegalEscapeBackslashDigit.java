/*
 * @test
 * @summary JLS 3.10.7: the character following a backslash must be one of
 *          b s t n f r " ' \ or an octal digit 0-7; 8 and 9 are not valid, so
 *          \8 and \9 are compile-time errors.
 * @compile/fail IllegalEscapeBackslashDigit.java
 */
public class IllegalEscapeBackslashDigit {
    String a = "\8";
    String b = "\9";
}
