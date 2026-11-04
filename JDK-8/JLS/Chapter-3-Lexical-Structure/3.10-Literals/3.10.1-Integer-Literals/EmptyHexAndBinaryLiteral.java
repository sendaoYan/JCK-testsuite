/*
 * @test
 * @summary JLS 3.10.1 (Java SE 8): a hexadecimal numeral must contain at least
 *          one hexadecimal digit and a binary numeral at least one binary
 *          digit; the bare prefixes 0x and 0b are compile-time errors.
 * @compile/fail EmptyHexAndBinaryLiteral.java
 */
public class EmptyHexAndBinaryLiteral {
    int a = 0x;
    int b = 0b;
}
