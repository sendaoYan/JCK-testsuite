/*
 * @test
 * @summary JLS 3.3: an eligible backslash followed by u whose last u is not
 *          followed by four hexadecimal digits is a compile-time error.
 * @compile/fail IncompleteUnicodeEscape.java
 */
public class IncompleteUnicodeEscape {
    int x = \u12;
}
