/*
 * @test
 * @summary java.util.function.DoubleUnaryOperator conformance: white-box structure + black-box behavior.
 * @run main DoubleUnaryOperatorTest
 */

import java.util.Comparator;
import java.util.function.*;

public class DoubleUnaryOperatorTest {
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
        Class<?> c = cls("java.util.function.DoubleUnaryOperator");
        check(c.isInterface(), "DoubleUnaryOperator is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "applyAsDouble", double.class);
        hasMethod(c, "compose", DoubleUnaryOperator.class);
        hasMethod(c, "andThen", DoubleUnaryOperator.class);
        hasMethod(c, "identity");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DoubleUnaryOperator inc = x -> x + 1.0;
        check(inc.applyAsDouble(4.0) == 5.0, "applyAsDouble");
        DoubleUnaryOperator dbl = x -> x * 2.0;
        check(inc.andThen(dbl).applyAsDouble(3.0) == 8.0, "andThen");
        check(inc.compose(dbl).applyAsDouble(3.0) == 7.0, "compose");
        check(DoubleUnaryOperator.identity().applyAsDouble(9.0) == 9.0, "identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
