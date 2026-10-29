/*
 * @test
 * @summary JLS 3.10.7: it is a compile-time error if the character following
 *          a backslash in an escape sequence is not a LineTerminator or one
 *          of b s t n f r " ' \ 0..7.
 * @compile/fail StringIllegalEscape.java
 */
public class StringIllegalEscape {
    String s = "\q";
}
