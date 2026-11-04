/*
 * @test
 * @summary java.util.function.BinaryOperator conformance: white-box structure + black-box behavior.
 * @run main BinaryOperatorTest
 */

import java.util.Comparator;
import java.util.function.*;

public class BinaryOperatorTest {
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
        Class<?> c = cls("java.util.function.BinaryOperator");
        check(c.isInterface(), "BinaryOperator is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "apply", Object.class, Object.class);
        hasMethod(c, "minBy", Comparator.class);
        hasMethod(c, "maxBy", Comparator.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BinaryOperator<Integer> add = Integer::sum;
        check(add.apply(3, 4) == 7, "apply");
        BinaryOperator<Integer> min = BinaryOperator.minBy(Integer::compareTo);
        check(min.apply(5, 3) == 3, "minBy");
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compareTo);
        check(max.apply(5, 3) == 5, "maxBy");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
