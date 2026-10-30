/*
 * @test
 * @summary JVMS 6.5: widening and narrowing numeric conversions (i2f, i2d,
 *          l2f, l2d, f2d, d2f, f2l, d2l, i2c).  i2c zero-extends to an
 *          unsigned 16-bit char; f2l/d2l truncate toward zero.
 * @compile Conv2.jasm
 * @run main Conv2Test
 */

public class Conv2Test {
    public static void main(String[] args) {
        if (Conv2.i2f(5) != 5.0f) throw new AssertionError("i2f");
        if (Conv2.i2d(5) != 5.0) throw new AssertionError("i2d");
        if (Conv2.l2f(123L) != 123.0f) throw new AssertionError("l2f");
        if (Conv2.l2d(123L) != 123.0) throw new AssertionError("l2d");
        if (Conv2.f2d(1.5f) != 1.5) throw new AssertionError("f2d");
        if (Conv2.d2f(1.5) != 1.5f) throw new AssertionError("d2f");

        if (Conv2.f2l(3.9f) != 3L) throw new AssertionError("f2l truncate");
        if (Conv2.d2l(-3.9) != -3L) throw new AssertionError("d2l truncate");

        if (Conv2.i2c(65) != 65) throw new AssertionError("i2c 65");
        if (Conv2.i2c(-1) != 0xFFFF) throw new AssertionError("i2c -1 -> 65535");

        System.out.println("PASS");
    }
}
