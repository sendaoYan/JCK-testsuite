/*
 * @test
 * @summary java.util.function.IntBinaryOperator conformance: white-box structure + black-box behavior.
 * @run main IntBinaryOperatorTest
 */

import java.util.Comparator;
import java.util.function.*;

public class IntBinaryOperatorTest {
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
        Class<?> c = cls("java.util.function.IntBinaryOperator");
        check(c.isInterface(), "IntBinaryOperator is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "applyAsInt", int.class, int.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        IntBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsInt(3, 4) == 7, "applyAsInt");
        IntBinaryOperator max = Integer::max;
        check(max.applyAsInt(9, 2) == 9, "applyAsInt max");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
