/*
 * @test
 * @summary java.util.function.LongUnaryOperator conformance: white-box structure + black-box behavior.
 * @run main LongUnaryOperatorTest
 */

import java.util.Comparator;
import java.util.function.*;

public class LongUnaryOperatorTest {
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
        Class<?> c = cls("java.util.function.LongUnaryOperator");
        check(c.isInterface(), "LongUnaryOperator is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "applyAsLong", long.class);
        hasMethod(c, "compose", LongUnaryOperator.class);
        hasMethod(c, "andThen", LongUnaryOperator.class);
        hasMethod(c, "identity");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LongUnaryOperator inc = x -> x + 1L;
        check(inc.applyAsLong(4L) == 5L, "applyAsLong");
        LongUnaryOperator dbl = x -> x * 2L;
        check(inc.andThen(dbl).applyAsLong(3L) == 8L, "andThen");
        check(inc.compose(dbl).applyAsLong(3L) == 7L, "compose");
        check(LongUnaryOperator.identity().applyAsLong(9L) == 9L, "identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
