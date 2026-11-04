/*
 * @test
 * @summary java.util.function.BiPredicate conformance: white-box structure + black-box behavior.
 * @run main BiPredicateTest
 */

import java.util.Comparator;
import java.util.function.*;

public class BiPredicateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.function.BiPredicate");
        check(c.isInterface(), "BiPredicate is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "test", Object.class, Object.class);
        hasMethod(c, "and", BiPredicate.class);
        hasMethod(c, "or", BiPredicate.class);
        hasMethod(c, "negate");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BiPredicate<Integer, Integer> gt = (a, b) -> a > b;
        check(gt.test(5, 3) && !gt.test(3, 5), "test");
        check(gt.negate().test(3, 5) && !gt.negate().test(5, 3), "negate");
        BiPredicate<Integer, Integer> neq = (a, b) -> a != b;
        check(gt.and(neq).test(5, 3) && !gt.and(neq).test(5, 5), "and");
        check(gt.or(neq).test(1, 3) && !gt.or(neq).test(3, 3), "or");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
