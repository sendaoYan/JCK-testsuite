/*
 * @test
 * @summary java.util.function.DoubleBinaryOperator API conformance: applyAsDouble.
 * @run main DoubleBinaryOperatorTest
 */

import java.util.function.DoubleBinaryOperator;

public class DoubleBinaryOperatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoubleBinaryOperator() {
        DoubleBinaryOperator div = (a, b) -> a / b;
        check(div.applyAsDouble(6.0, 2.0) == 3.0, "applyAsDouble");
    }

    public static void main(String[] args) {
        testDoubleBinaryOperator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
