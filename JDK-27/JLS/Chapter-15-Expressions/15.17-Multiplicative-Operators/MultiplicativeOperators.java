/*
 * @test
 * @summary JLS 15.17: multiplication, division and remainder. Integer division
 *          truncates toward zero, remainder takes the sign of the dividend, and
 *          integer division/remainder by zero throws ArithmeticException.
 * @run main MultiplicativeOperators
 */

public class MultiplicativeOperators {
    public static void main(String[] args) {
        if (6 * 7 != 42) throw new AssertionError("*");
        if (7 / 2 != 3) throw new AssertionError("7 / 2");
        if (-7 / 2 != -3) throw new AssertionError("-7 / 2 (truncate toward zero)");
        if (7 % 2 != 1) throw new AssertionError("7 % 2");
        if (-7 % 2 != -1) throw new AssertionError("-7 % 2 (sign of dividend)");
        if (7 % -2 != 1) throw new AssertionError("7 % -2 (sign of dividend)");

        try {
            int x = 1 / 0;
            throw new AssertionError("1/0 did not throw");
        } catch (ArithmeticException e) {
            // expected
        }

        if (7.0 / 2 != 3.5) throw new AssertionError("floating-point division");

        System.out.println("PASS");
    }
}
