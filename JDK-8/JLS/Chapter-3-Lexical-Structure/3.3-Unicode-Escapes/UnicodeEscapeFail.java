/*
 * @test
 * @summary JLS 3.3 (Java SE 8): a Unicode escape must be followed by exactly
 *          four hexadecimal digits; a malformed escape is a compile-time error.
 * @compile/fail UnicodeEscapeFail.java
 */

class UnicodeEscapeFail {
    int x = '\u00G1';   // error: illegal unicode escape
}
