/*
 * @test
 * @summary java.util.function.Predicate conformance: white-box structure + black-box behavior.
 * @run main PredicateTest
 */

import java.util.Comparator;
import java.util.function.*;

public class PredicateTest {
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
        Class<?> c = cls("java.util.function.Predicate");
        check(c.isInterface(), "Predicate is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "test", Object.class);
        hasMethod(c, "and", Predicate.class);
        hasMethod(c, "or", Predicate.class);
        hasMethod(c, "negate");
        hasMethod(c, "isEqual", Object.class);
        hasMethod(c, "not", Predicate.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Predicate<Integer> pos = x -> x > 0;
        check(pos.test(1) && !pos.test(-1), "test");
        check(pos.negate().test(-1) && !pos.negate().test(1), "negate");
        Predicate<Integer> even = x -> x % 2 == 0;
        check(pos.and(even).test(2) && !pos.and(even).test(3), "and");
        check(pos.or(even).test(-2) && !pos.or(even).test(-1), "or");
        check(Predicate.isEqual("a").test("a") && !Predicate.isEqual("a").test("b"), "isEqual");
        check(Predicate.not(pos).test(-1) && !Predicate.not(pos).test(1), "not");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
