/*
 * @test
 * @summary java.util.function.UnaryOperator conformance: white-box structure + black-box behavior.
 * @run main UnaryOperatorTest
 */

import java.util.Comparator;
import java.util.function.*;

public class UnaryOperatorTest {
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
        Class<?> c = cls("java.util.function.UnaryOperator");
        check(c.isInterface(), "UnaryOperator is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "apply", Object.class);
        hasMethod(c, "identity");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        UnaryOperator<Integer> sq = x -> x * x;
        check(sq.apply(5) == 25, "apply");
        check(UnaryOperator.identity().apply("z").equals("z"), "identity");
        UnaryOperator<Integer> plusOne = x -> x + 1;
        check(sq.andThen(plusOne).apply(3) == 10, "andThen");
        check(sq.compose(plusOne).apply(3) == 16, "compose");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
