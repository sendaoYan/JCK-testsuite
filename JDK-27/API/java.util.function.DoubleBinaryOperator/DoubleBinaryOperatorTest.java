/*
 * @test
 * @summary java.util.function.DoubleBinaryOperator conformance: white-box structure + black-box behavior.
 * @run main DoubleBinaryOperatorTest
 */

import java.util.Comparator;
import java.util.function.*;

public class DoubleBinaryOperatorTest {
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
        Class<?> c = cls("java.util.function.DoubleBinaryOperator");
        check(c.isInterface(), "DoubleBinaryOperator is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "applyAsDouble", double.class, double.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DoubleBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsDouble(3.0, 4.0) == 7.0, "applyAsDouble");
        DoubleBinaryOperator max = Double::max;
        check(max.applyAsDouble(9.0, 2.0) == 9.0, "applyAsDouble max");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
