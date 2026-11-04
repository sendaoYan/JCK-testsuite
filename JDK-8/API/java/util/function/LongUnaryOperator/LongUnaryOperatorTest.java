/*
 * @test
 * @summary java.util.function.LongUnaryOperator API conformance: applyAsLong/andThen/compose/identity.
 * @run main LongUnaryOperatorTest
 */

import java.util.function.LongUnaryOperator;

public class LongUnaryOperatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongUnaryOperator() {
        LongUnaryOperator inc = x -> x + 1;
        check(inc.applyAsLong(5L) == 6L, "applyAsLong");
        check(inc.andThen(x -> x * 10).applyAsLong(5L) == 60L, "andThen");
        check(inc.compose(x -> x * 10).applyAsLong(5L) == 51L, "compose");
        check(LongUnaryOperator.identity().applyAsLong(7L) == 7L, "identity");
    }

    public static void main(String[] args) {
        testLongUnaryOperator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
