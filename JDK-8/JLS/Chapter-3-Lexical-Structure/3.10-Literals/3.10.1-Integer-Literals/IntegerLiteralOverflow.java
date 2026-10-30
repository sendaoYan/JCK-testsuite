/*
 * @test
 * @summary JLS 3.10.1 (Java SE 8): a decimal integer literal larger than
 *          2147483647 without a long suffix is a compile-time error.
 * @compile/fail IntegerLiteralOverflow.java
 */

class IntegerLiteralOverflow {
    int a = 2147483648;        // error: integer number too large
    int b = 999999999999;      // error: integer number too large
}
