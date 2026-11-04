/*
 * @test
 * @summary java.util.function.IntPredicate conformance: white-box structure + black-box behavior.
 * @run main IntPredicateTest
 */

import java.util.Comparator;
import java.util.function.*;

public class IntPredicateTest {
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
        Class<?> c = cls("java.util.function.IntPredicate");
        check(c.isInterface(), "IntPredicate is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "test", int.class);
        hasMethod(c, "and", IntPredicate.class);
        hasMethod(c, "or", IntPredicate.class);
        hasMethod(c, "negate");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        IntPredicate pos = x -> x > 0;
        check(pos.test(1) && !pos.test(-1), "test");
        check(pos.negate().test(-1) && !pos.negate().test(1), "negate");
        IntPredicate even = x -> x % 2 == 0;
        check(pos.and(even).test(2) && !pos.and(even).test(3), "and");
        check(pos.or(even).test(-2) && !pos.or(even).test(-1), "or");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
