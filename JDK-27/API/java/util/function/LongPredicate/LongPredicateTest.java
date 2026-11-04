/*
 * @test
 * @summary java.util.function.LongPredicate conformance: white-box structure + black-box behavior.
 * @run main LongPredicateTest
 */

import java.util.Comparator;
import java.util.function.*;

public class LongPredicateTest {
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
        Class<?> c = cls("java.util.function.LongPredicate");
        check(c.isInterface(), "LongPredicate is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "test", long.class);
        hasMethod(c, "and", LongPredicate.class);
        hasMethod(c, "or", LongPredicate.class);
        hasMethod(c, "negate");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LongPredicate pos = x -> x > 0L;
        check(pos.test(1L) && !pos.test(-1L), "test");
        check(pos.negate().test(-1L) && !pos.negate().test(1L), "negate");
        LongPredicate even = x -> x % 2L == 0L;
        check(pos.and(even).test(2L) && !pos.and(even).test(3L), "and");
        check(pos.or(even).test(-2L) && !pos.or(even).test(-1L), "or");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
