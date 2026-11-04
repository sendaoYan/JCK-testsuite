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

    // ===================== 契约深化：and/or/negate 全真值表/短路/NPE/边界值 =====================
    static void testContracts() {
        IntPredicate even = n -> (n & 1) == 0;
        check(even.test(0) && even.test(-2) && !even.test(3), "even incl. 0 and negatives");
        IntPredicate nonNeg = n -> n >= 0;
        check(even.and(nonNeg).test(4) && !even.and(nonNeg).test(-4) && !even.and(nonNeg).test(3),
              "and combos");
        boolean[] vals = {true, false};
        for (boolean p : vals) for (boolean q : vals) {
            IntPredicate a = n -> p, b = n -> q;
            check(a.and(b).test(0) == (p && q), "and table " + p + q);
            check(a.or(b).test(0) == (p || q), "or table " + p + q);
            check(a.negate().test(0) == !p, "negate table " + p);
        }
        final int[] calls = {0};
        IntPredicate slow = n -> { calls[0]++; return true; };
        ((IntPredicate) n -> false).and(slow).test(0);
        check(calls[0] == 0, "and short-circuit");
        ((IntPredicate) n -> true).or(slow).test(0);
        check(calls[0] == 0, "or short-circuit");
        checkThrows(NullPointerException.class, () -> even.and(null), "and(null) NPE");
        checkThrows(NullPointerException.class, () -> even.or(null), "or(null) NPE");
        // 边界
        IntPredicate isMin = n -> n == Integer.MIN_VALUE;
        check(isMin.test(Integer.MIN_VALUE) && !isMin.test(Integer.MAX_VALUE), "MIN/MAX boundary");
        check(even.negate().negate().test(2), "double negate");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
