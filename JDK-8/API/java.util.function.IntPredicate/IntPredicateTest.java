/*
 * @test
 * @summary java.util.function.IntPredicate API conformance: test, negate, and, and or.
 * @run main IntPredicateTest
 */

import java.util.function.IntPredicate;

public class IntPredicateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntPredicate() {
        IntPredicate pos = x -> x > 0;
        check(pos.test(1) && !pos.test(-1), "test");
        check(pos.negate().test(-1), "negate");
        check(pos.and(x -> x < 10).test(5) && !pos.and(x -> x < 10).test(20), "and");
        check(pos.or(x -> x == -1).test(-1), "or");
    }

    public static void main(String[] args) {
        testIntPredicate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
