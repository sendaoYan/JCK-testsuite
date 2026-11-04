/*
 * @test
 * @summary java.util.function.BiPredicate API conformance: test/and/or/negate.
 * @run main BiPredicateTest
 */

import java.util.function.BiPredicate;

public class BiPredicateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBiPredicate() {
        BiPredicate<Integer, Integer> gt = (a, b) -> a > b;
        check(gt.test(3, 2), "test true");
        check(!gt.test(2, 3), "test false");
        check(gt.and((a, b) -> a != b).test(3, 2), "and");
        check(!gt.and((a, b) -> a != b).test(3, 3), "and false");
        check(gt.or((a, b) -> a == b).test(2, 2), "or");
        check(gt.negate().test(2, 3), "negate");
    }

    public static void main(String[] args) {
        testBiPredicate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
