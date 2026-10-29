/*
 * @test
 * @summary JLS 3.10.1: underscores are not allowed immediately after 0x/0b or
 *          after the last digit of a numeral, nor before the first digit of a
 *          decimal/octal numeral.
 * @compile/fail IntegerLiteralBadUnderscores.java
 */

public class IntegerLiteralBadUnderscores {
    int a = 0_x1;       // underscore immediately after 0x
    int b = 0b_1;       // underscore immediately after 0b
    int c = 1_;         // underscore after the last digit
    int d = _1;         // underscore before the first digit (decimal)
    int e = 0x_1;       // underscore immediately after 0x (hex)
    int f = 0x7fff_;    // underscore after the last hex digit
}
