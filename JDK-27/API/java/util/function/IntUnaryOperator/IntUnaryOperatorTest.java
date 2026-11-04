/*
 * @test
 * @summary java.util.function.IntUnaryOperator conformance: white-box structure + black-box behavior.
 * @run main IntUnaryOperatorTest
 */

import java.util.Comparator;
import java.util.function.*;

public class IntUnaryOperatorTest {
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
        Class<?> c = cls("java.util.function.IntUnaryOperator");
        check(c.isInterface(), "IntUnaryOperator is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "applyAsInt", int.class);
        hasMethod(c, "compose", IntUnaryOperator.class);
        hasMethod(c, "andThen", IntUnaryOperator.class);
        hasMethod(c, "identity");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        IntUnaryOperator inc = x -> x + 1;
        check(inc.applyAsInt(4) == 5, "applyAsInt");
        IntUnaryOperator dbl = x -> x * 2;
        check(inc.andThen(dbl).applyAsInt(3) == 8, "andThen");
        check(inc.compose(dbl).applyAsInt(3) == 7, "compose");
        check(IntUnaryOperator.identity().applyAsInt(9) == 9, "identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
