/*
 * @test
 * @summary java.util.function.DoubleUnaryOperator API conformance: applyAsDouble/andThen/compose/identity.
 * @run main DoubleUnaryOperatorTest
 */

import java.util.function.DoubleUnaryOperator;

public class DoubleUnaryOperatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoubleUnaryOperator() {
        DoubleUnaryOperator twice = x -> x * 2;
        check(twice.applyAsDouble(2.5) == 5.0, "applyAsDouble");
        check(twice.andThen(x -> x + 1).applyAsDouble(2.5) == 6.0, "andThen");
        check(twice.compose(x -> x + 1).applyAsDouble(2.5) == 7.0, "compose");
        check(DoubleUnaryOperator.identity().applyAsDouble(7.5) == 7.5, "identity");
    }

    public static void main(String[] args) {
        testDoubleUnaryOperator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
