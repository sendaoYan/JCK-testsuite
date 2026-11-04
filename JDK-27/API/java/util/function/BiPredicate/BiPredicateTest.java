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

    // ===================== 契约深化：and/or/negate 全真值表/短路/链式 =====================
    static void testContracts() {
        boolean[] vals = {true, false};
        for (boolean p : vals) for (boolean q : vals) {
            BiPredicate<String, String> a = (x, y) -> p;
            BiPredicate<String, String> b = (x, y) -> q;
            check(a.and(b).test("x", "y") == (p && q), "and truth table " + p + "," + q);
            check(a.or(b).test("x", "y") == (p || q), "or truth table " + p + "," + q);
            check(a.negate().test("x", "y") == !p, "negate truth table " + p);
        }
        final int[] calls = {0};
        BiPredicate<String, String> slow = (x, y) -> { calls[0]++; return true; };
        BiPredicate<String, String> never = (x, y) -> false;
        check(!never.and(slow).test("x", "y"), "and left false");
        check(calls[0] == 0, "and right not evaluated after left false");
        BiPredicate<String, String> always = (x, y) -> true;
        check(always.or(slow).test("x", "y"), "or left true");
        check(calls[0] == 0, "or right not evaluated after left true");
        checkThrows(NullPointerException.class, () -> always.and(null), "and(null) NPE");
        checkThrows(NullPointerException.class, () -> always.or(null), "or(null) NPE");
        // and().or() 组合：and 先结合
        BiPredicate<String, String> eq = (x, y) -> x.equals(y);
        BiPredicate<String, String> bothLen1 = (x, y) -> x.length() == 1 && y.length() == 1;
        BiPredicate<String, String> startsA = (x, y) -> x.startsWith("a");
        check(eq.and(bothLen1).or(startsA).test("a", "z"), "(eq and len1) or startsA");
        check(!eq.and(bothLen1).test("ab", "ab"), "eq and len1 false");
        // negate 自反
        BiPredicate<String, String> n2 = eq.negate().negate();
        check(n2.test("q", "q"), "double negate identity");
        // 异常透传
        RuntimeException boom = new RuntimeException("bp");
        try {
            ((BiPredicate<String, String>) (x, y) -> { throw boom; }).and(eq).test("x", "y");
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception relayed");
        }
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
