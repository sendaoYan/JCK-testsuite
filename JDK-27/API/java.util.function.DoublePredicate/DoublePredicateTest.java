/*
 * @test
 * @summary java.util.function.DoublePredicate conformance: white-box structure + black-box behavior.
 * @run main DoublePredicateTest
 */

import java.util.Comparator;
import java.util.function.*;

public class DoublePredicateTest {
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
        Class<?> c = cls("java.util.function.DoublePredicate");
        check(c.isInterface(), "DoublePredicate is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "test", double.class);
        hasMethod(c, "and", DoublePredicate.class);
        hasMethod(c, "or", DoublePredicate.class);
        hasMethod(c, "negate");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DoublePredicate pos = x -> x > 0.0;
        check(pos.test(1.0) && !pos.test(-1.0), "test");
        check(pos.negate().test(-1.0) && !pos.negate().test(1.0), "negate");
        DoublePredicate whole = x -> x == Math.floor(x);
        check(pos.and(whole).test(2.0) && !pos.and(whole).test(1.5), "and");
        check(pos.or(whole).test(-2.0) && !pos.or(whole).test(-1.5), "or");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
