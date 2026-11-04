/*
 * @test
 * @summary JLS 3.10.1: an octal numeral consists only of octal digits (0-7);
 *          the digits 8 and 9 are not octal digits, so 08 and 09 are
 *          compile-time errors ("illegal digit in an octal literal").
 * @compile/fail OctalDigitOutOfRange.java
 */
public class OctalDigitOutOfRange {
    int a = 08;
    int b = 09;
}
