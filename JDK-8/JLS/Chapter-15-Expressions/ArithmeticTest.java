/*
 * @test
 * @summary JLS 15.17-15.19 (Java SE 8): multiplicative, additive, shift, and
 *          bitwise operators, including integer overflow wrap-around.
 * @run main ArithmeticTest
 */

public class ArithmeticTest {
    public static void main(String[] args) {
        if (7 / 2 != 3 || 7 % 2 != 1) throw new AssertionError("integer division/modulo");
        if (-7 / 2 != -3 || -7 % 2 != -1) throw new AssertionError("truncation toward zero");
        if (2 * 3 + 4 != 10) throw new AssertionError("precedence");
        if (Integer.MAX_VALUE + 1 != Integer.MIN_VALUE) throw new AssertionError("int overflow wrap");
        if (Integer.MIN_VALUE - 1 != Integer.MAX_VALUE) throw new AssertionError("int underflow wrap");

        if (1 << 4 != 16) throw new AssertionError("left shift");
        if (16 >> 2 != 4) throw new AssertionError("right shift");
        if (-16 >> 2 != -4) throw new AssertionError("signed right shift");
        if (-1 >>> 28 != 0x0F) throw new AssertionError("unsigned right shift");

        if ((5 & 3) != 1 || (5 | 3) != 7 || (5 ^ 3) != 6) throw new AssertionError("bitwise");

        System.out.println("PASS");
    }
}
