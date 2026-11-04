/*
 * @test
 * @summary java.util.function.DoublePredicate contract: test, and/or/negate truth table, short-circuit.
 * @run main DoublePredicateTest
 */

import java.util.function.DoublePredicate;

public class DoublePredicateTest {
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
        DoublePredicate pos = x -> x > 0;
        check(pos.test(6.0), "test positive");
        check(!pos.test(-1.0), "test non-positive");
        DoublePredicate even = x -> x % 2 == 0;
        check(pos.and(even).test(6.0), "and true");
        check(!pos.and(even).test(5.0), "and false");
        check(pos.or(even).test(5.0), "or true");
        check(!pos.or(even).test(-1.0), "or false");
        check(!pos.negate().test(6.0), "negate true->false");
        check(pos.negate().test(-1.0), "negate false->true");
        check(pos.negate().negate().test(6.0), "double negate");
        checkThrows(NullPointerException.class, () -> pos.and(null), "and(null)");
        checkThrows(NullPointerException.class, () -> pos.or(null), "or(null)");
        final int[] calls = new int[1];
        DoublePredicate slow = x -> { calls[0]++; return true; };
        check(!((DoublePredicate) x -> false).and(slow).test(6.0), "and short-circuit");
        check(calls[0] == 0, "and right operand not called");
        check(((DoublePredicate) x -> true).or(slow).test(6.0), "or short-circuit");
        check(calls[0] == 0, "or right operand not called");
    }

    // ===================== 契约深化：and/or/negate 全真值表/短路/NPE/NaN 判定 =====================
    static void testContracts() {
        DoublePredicate isNaN = Double::isNaN;
        check(isNaN.test(Double.NaN) && !isNaN.test(0.0), "isNaN method ref");
        DoublePredicate pos = d -> d > 0;
        check(pos.and(isNaN).test(1.0) == false, "and with NaN predicate");
        check(isNaN.or(pos).test(5.0), "or true");
        boolean[] vals = {true, false};
        for (boolean p : vals) for (boolean q : vals) {
            DoublePredicate a = d -> p, b = d -> q;
            check(a.and(b).test(0) == (p && q), "and table " + p + q);
            check(a.or(b).test(0) == (p || q), "or table " + p + q);
            check(a.negate().test(0) == !p, "negate table " + p);
        }
        final int[] calls = {0};
        DoublePredicate slow = d -> { calls[0]++; return true; };
        ((DoublePredicate) d -> false).and(slow).test(0);
        check(calls[0] == 0, "and short-circuit");
        ((DoublePredicate) d -> true).or(slow).test(0);
        check(calls[0] == 0, "or short-circuit");
        checkThrows(NullPointerException.class, () -> pos.and(null), "and(null) NPE");
        checkThrows(NullPointerException.class, () -> pos.or(null), "or(null) NPE");
        check(pos.negate().negate().test(1.0), "double negate");
        // -0.0 vs 0.0 语义
        DoublePredicate negZero = d -> 1 / d == Double.NEGATIVE_INFINITY;
        check(negZero.test(-0.0) && !negZero.test(0.0), "signed zero distinguishable");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
