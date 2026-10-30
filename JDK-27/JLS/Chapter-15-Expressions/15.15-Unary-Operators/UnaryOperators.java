/*
 * @test
 * @summary JLS 15.15: unary plus, minus, bitwise complement and logical
 *          complement operators; unary minus on the minimum value wraps around.
 * @run main UnaryOperators
 */

public class UnaryOperators {
    public static void main(String[] args) {
        if (+5 != 5) throw new AssertionError("unary plus");
        if (-5 != -5) throw new AssertionError("unary minus");
        if (~0 != -1) throw new AssertionError("bitwise complement of 0");
        if (~5 != -6) throw new AssertionError("bitwise complement of 5");
        if (!true) throw new AssertionError("logical complement");
        if (!!true != true) throw new AssertionError("double logical complement");
        if (-Integer.MIN_VALUE != Integer.MIN_VALUE)
            throw new AssertionError("unary minus overflow wraps");

        System.out.println("PASS");
    }
}
