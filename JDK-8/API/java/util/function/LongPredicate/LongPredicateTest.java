/*
 * @test
 * @summary java.util.function.LongPredicate API conformance: test/and/or/negate.
 * @run main LongPredicateTest
 */

import java.util.function.LongPredicate;

public class LongPredicateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongPredicate() {
        LongPredicate even = l -> l % 2 == 0;
        check(even.test(4L), "test true");
        check(!even.test(3L), "test false");
        check(even.negate().test(3L), "negate");
        check(even.and(l -> l > 0).test(4L), "and");
        check(!even.and(l -> l > 0).test(-4L), "and short-circuits to false");
        check(even.or(l -> l > 2).test(3L), "or");
    }

    public static void main(String[] args) {
        testLongPredicate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
