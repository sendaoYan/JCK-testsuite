/*
 * @test
 * @summary java.util.function.IntUnaryOperator API conformance: applyAsInt/andThen/compose/identity.
 * @run main IntUnaryOperatorTest
 */

import java.util.function.IntUnaryOperator;

public class IntUnaryOperatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntUnaryOperator() {
        IntUnaryOperator add2 = x -> x + 2;
        check(add2.applyAsInt(3) == 5, "applyAsInt");
        check(add2.andThen(x -> x * 10).applyAsInt(3) == 50, "andThen");
        check(add2.compose(x -> x * 10).applyAsInt(3) == 32, "compose");
        check(IntUnaryOperator.identity().applyAsInt(7) == 7, "identity");
    }

    public static void main(String[] args) {
        testIntUnaryOperator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
