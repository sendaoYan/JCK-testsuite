/*
 * @test
 * @summary java.util.function.LongBinaryOperator conformance: white-box structure + black-box behavior.
 * @run main LongBinaryOperatorTest
 */

import java.util.Comparator;
import java.util.function.*;

public class LongBinaryOperatorTest {
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
        Class<?> c = cls("java.util.function.LongBinaryOperator");
        check(c.isInterface(), "LongBinaryOperator is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "applyAsLong", long.class, long.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LongBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsLong(3L, 4L) == 7L, "applyAsLong");
        LongBinaryOperator max = Long::max;
        check(max.applyAsLong(9L, 2L) == 9L, "applyAsLong max");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
