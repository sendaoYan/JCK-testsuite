/*
 * @test
 * @summary java.util.function.BiPredicate contract: test, and/or/negate truth table, short-circuit, null->NPE.
 * @run main BiPredicateTest
 */

import java.util.function.BiPredicate;

public class BiPredicateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }
    static void testMain() {
        BiPredicate<Integer, Integer> less = (a, b) -> a < b;
        check(less.test(1, 2), "test");
        check(!less.test(2, 1), "test false");
        BiPredicate<Integer, Integer> eq = (a, b) -> a.equals(b);
        check(!less.and(eq).test(1, 1), "and false");
        check(less.or(eq).test(1, 1), "or true");
        check(!less.or(eq).test(2, 1), "or false");
        check(!less.negate().test(1, 2), "negate true->false");
        check(less.negate().test(2, 1), "negate false->true");
        checkThrows(NullPointerException.class, () -> less.and(null), "and(null)");
        checkThrows(NullPointerException.class, () -> less.or(null), "or(null)");
        final int[] calls = new int[1];
        BiPredicate<Integer,Integer> slow = (a, b) -> { calls[0]++; return true; };
        BiPredicate<Integer,Integer> alwaysFalse = (a, b) -> false;
        BiPredicate<Integer,Integer> alwaysTrue = (a, b) -> true;
        check(!alwaysFalse.and(slow).test(1, 2), "and short-circuit");
        check(calls[0] == 0, "and right operand not called");
        check(alwaysTrue.or(slow).test(1, 2), "or short-circuit");
        check(calls[0] == 0, "or right operand not called");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
