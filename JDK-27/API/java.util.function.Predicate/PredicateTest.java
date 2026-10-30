/*
 * @test
 * @summary java.util.function.Predicate API conformance: test, negate, and, or, and
 *          static isEqual.
 * @run main PredicateTest
 */

import java.util.function.Predicate;

public class PredicateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPredicate() {
        Predicate<Integer> pos = x -> x > 0;
        check(pos.test(1) && !pos.test(-1), "test");
        check(pos.negate().test(-1) && !pos.negate().test(1), "negate");
        Predicate<Integer> even = x -> x % 2 == 0;
        check(pos.and(even).test(2) && !pos.and(even).test(3), "and");
        check(pos.or(even).test(-2) && !pos.or(even).test(-1), "or");
        check(Predicate.isEqual("a").test("a") && !Predicate.isEqual("a").test("b"), "isEqual");
    }

    public static void main(String[] args) {
        testPredicate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
