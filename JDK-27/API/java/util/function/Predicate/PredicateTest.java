/*
 * @test
 * @summary java.util.function.Predicate contract: test, and/or/negate truth table, isEqual, short-circuit, null->NPE.
 * @run main PredicateTest
 */

import java.util.function.Predicate;

public class PredicateTest {
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
        Predicate<String> empty = String::isEmpty;
        check(empty.test(""), "test empty");
        check(!empty.test("x"), "test non-empty");
        Predicate<String> startsA = s -> s.startsWith("a");
        check(!startsA.and(empty).test(""), "and left false short-circuits");
        check(!startsA.and(empty).test("ab"), "and true/false");
        check(startsA.or(empty).test("ab"), "or true");
        check(!startsA.or(empty).test("b"), "or false");
        check(!startsA.negate().test("ab"), "negate true->false");
        check(startsA.negate().test("b"), "negate false->true");
        check(startsA.negate().negate().test("ab"), "double negate");
        checkThrows(NullPointerException.class, () -> startsA.and(null), "and(null)");
        checkThrows(NullPointerException.class, () -> startsA.or(null), "or(null)");
        check(Predicate.not(startsA).test("b"), "not true");
        check(!Predicate.not(startsA).test("ab"), "not false");
        checkThrows(NullPointerException.class, () -> Predicate.not(null), "not(null)");
        Predicate<Object> eqNull = Predicate.isEqual(null);
        check(eqNull.test(null), "isEqual(null).test(null)");
        check(!eqNull.test("x"), "isEqual(null).test(x)");
        check(Predicate.isEqual("a").test("a"), "isEqual equal");
        check(!Predicate.isEqual("a").test("b"), "isEqual not equal");
        final int[] calls = new int[1];
        Predicate<Integer> slow = x -> { calls[0]++; return true; };
        Predicate<Integer> alwaysFalse = x -> false;
        Predicate<Integer> alwaysTrue = x -> true;
        check(!alwaysFalse.and(slow).test(1), "and short-circuit false");
        check(calls[0] == 0, "and right operand not called after left false");
        check(alwaysTrue.or(slow).test(1), "or short-circuit true");
        check(calls[0] == 0, "or right operand not called after left true");
    }

    // ===================== 契约深化：全真值表/链式优先级/isEqual 类型混用/异常透传/not 存在性 =====================
    static void testContracts() {
        boolean[] vals = {true, false};
        for (boolean p : vals) for (boolean q : vals) {
            Predicate<Integer> a = x -> p, b = x -> q;
            check(a.and(b).test(0) == (p && q), "and full table " + p + q);
            check(a.or(b).test(0) == (p || q), "or full table " + p + q);
            check(a.negate().test(0) == !p, "negate table " + p);
        }
        Predicate<String> len3 = s -> s.length() == 3;
        Predicate<String> startsB = s -> s.startsWith("b");
        check(len3.and(startsB).or(s -> s.isEmpty()).test(""), "(len3 and startsB) or empty");
        check(len3.negate().negate().negate().test("abc") == false, "triple negate flips");
        // isEqual 用 equals 语义（不同类型对象）
        Predicate<Object> eq5 = Predicate.isEqual(5);
        check(!eq5.test("5"), "isEqual uses equals not toString");
        check(eq5.test(5), "Integer equality via autobox cache in this range");
        // 异常透传
        RuntimeException boom = new RuntimeException("pr");
        try {
            Predicate<String> thrower = s -> { throw boom; };
            thrower.and(len3).test("x");
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception relayed");
        }
        // JDK-11+ 静态 not()（版本面锁定，行为在 testMain 已覆盖）
        try {
            java.lang.reflect.Method not = Predicate.class.getMethod("not", Predicate.class);
            check(not.getReturnType() == Predicate.class, "static not(Predicate) present, returns Predicate");
            check(java.lang.reflect.Modifier.isStatic(not.getModifiers()), "not is static");
        } catch (NoSuchMethodException e) {
            check(false, "static not(Predicate) must be present in this JDK");
        }
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
