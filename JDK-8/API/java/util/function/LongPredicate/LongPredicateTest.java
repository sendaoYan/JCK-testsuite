/*
 * @test
 * @summary java.util.function.LongPredicate contract: test, and/or/negate truth table, short-circuit.
 * @run main LongPredicateTest
 */

import java.util.function.LongPredicate;

public class LongPredicateTest {
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
        LongPredicate pos = x -> x > 0;
        check(pos.test(6L), "test positive");
        check(!pos.test(-1L), "test non-positive");
        LongPredicate even = x -> x % 2 == 0;
        check(pos.and(even).test(6L), "and true");
        check(!pos.and(even).test(5L), "and false");
        check(pos.or(even).test(5L), "or true");
        check(!pos.or(even).test(-1L), "or false");
        check(!pos.negate().test(6L), "negate true->false");
        check(pos.negate().test(-1L), "negate false->true");
        check(pos.negate().negate().test(6L), "double negate");
        checkThrows(NullPointerException.class, () -> pos.and(null), "and(null)");
        checkThrows(NullPointerException.class, () -> pos.or(null), "or(null)");
        final int[] calls = new int[1];
        LongPredicate slow = x -> { calls[0]++; return true; };
        check(!((LongPredicate) x -> false).and(slow).test(6L), "and short-circuit");
        check(calls[0] == 0, "and right operand not called");
        check(((LongPredicate) x -> true).or(slow).test(6L), "or short-circuit");
        check(calls[0] == 0, "or right operand not called");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
