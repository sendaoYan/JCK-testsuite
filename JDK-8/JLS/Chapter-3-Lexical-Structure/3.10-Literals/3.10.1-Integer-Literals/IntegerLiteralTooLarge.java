/*
 * @test
 * @summary JLS 3.10.1 (Java SE 8): it is a compile-time error if a decimal int
 *          literal is larger than 2147483648 (or 2147483648 appears other than as
 *          the operand of unary minus), if a hex/octal/binary int literal does not
 *          fit in 32 bits, or if a long literal is out of 64-bit range.
 * @compile/fail IntegerLiteralTooLarge.java
 */

public class IntegerLiteralTooLarge {
    int a = 2147483648;                     // 2^31, not operand of unary minus
    int b = 3000000000;                     // larger than 2^31
    int c = 0x1_0000_0000;                  // hex int does not fit in 32 bits
    int d = 040000000000;                   // octal int does not fit in 32 bits
    int e = 0b1_0000_0000_0000_0000_0000_0000_0000_0000; // binary int > 32 bits
    long f = 9223372036854775808L;          // 2^63, not operand of unary minus
    long g = 0x1_0000_0000_0000_0000L;      // hex long does not fit in 64 bits
    long h = 01000000000000000000000L;      // octal long does not fit in 64 bits
}
