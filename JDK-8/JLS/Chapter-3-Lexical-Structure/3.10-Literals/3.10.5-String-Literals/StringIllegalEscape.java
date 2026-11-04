/*
 * @test
 * @summary JLS 3.10.7 (Java SE 8): it is a compile-time error if the character
 *          following a backslash in an escape sequence is not one of
 *          b t n f r " ' \ or an octal digit.  In Java SE 8 there is no \s
 *          escape (that was added later), and 8/9 are not octal digits, so
 *          \q, \s, \8, and \9 are all compile-time errors.
 * @compile/fail StringIllegalEscape.java
 */
public class StringIllegalEscape {
    String a = "\q";
    String b = "\s";
    String c = "\8";
    String d = "\9";
}
