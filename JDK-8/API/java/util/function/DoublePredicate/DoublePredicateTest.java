/*
 * @test
 * @summary java.util.function.DoublePredicate API conformance: test/and/or/negate.
 * @run main DoublePredicateTest
 */

import java.util.function.DoublePredicate;

public class DoublePredicateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoublePredicate() {
        DoublePredicate even = d -> d % 2 == 0;
        check(even.test(4.0), "test true");
        check(!even.test(3.0), "test false");
        check(even.negate().test(3.0), "negate");
        check(even.and(d -> d > 0).test(4.0), "and");
        check(!even.and(d -> d > 0).test(-4.0), "and short-circuits to false");
        check(even.or(d -> d > 2).test(3.0), "or");
    }

    public static void main(String[] args) {
        testDoublePredicate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
