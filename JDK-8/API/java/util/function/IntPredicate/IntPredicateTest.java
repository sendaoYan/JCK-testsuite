/*
 * @test
 * @summary java.util.function.IntPredicate contract: test, and/or/negate truth table, short-circuit.
 * @run main IntPredicateTest
 */

import java.util.function.IntPredicate;

public class IntPredicateTest {
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
        IntPredicate pos = x -> x > 0;
        check(pos.test(6), "test positive");
        check(!pos.test(-1), "test non-positive");
        IntPredicate even = x -> x % 2 == 0;
        check(pos.and(even).test(6), "and true");
        check(!pos.and(even).test(5), "and false");
        check(pos.or(even).test(5), "or true");
        check(!pos.or(even).test(-1), "or false");
        check(!pos.negate().test(6), "negate true->false");
        check(pos.negate().test(-1), "negate false->true");
        check(pos.negate().negate().test(6), "double negate");
        checkThrows(NullPointerException.class, () -> pos.and(null), "and(null)");
        checkThrows(NullPointerException.class, () -> pos.or(null), "or(null)");
        final int[] calls = new int[1];
        IntPredicate slow = x -> { calls[0]++; return true; };
        check(!((IntPredicate) x -> false).and(slow).test(6), "and short-circuit");
        check(calls[0] == 0, "and right operand not called");
        check(((IntPredicate) x -> true).or(slow).test(6), "or short-circuit");
        check(calls[0] == 0, "or right operand not called");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
