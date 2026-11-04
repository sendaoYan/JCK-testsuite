/*
 * @test
 * @summary java.util.function.BiFunction conformance: white-box structure + black-box behavior.
 * @run main BiFunctionTest
 */

import java.util.Comparator;
import java.util.function.*;

public class BiFunctionTest {
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
        Class<?> c = cls("java.util.function.BiFunction");
        check(c.isInterface(), "BiFunction is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "apply", Object.class, Object.class);
        hasMethod(c, "andThen", Function.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BiFunction<String, String, Integer> lenSum = (a, b) -> a.length() + b.length();
        check(lenSum.apply("ab", "cde") == 5, "apply");
        Function<Integer, Integer> neg = x -> -x;
        check(lenSum.andThen(neg).apply("a", "bc") == -3, "andThen");
        BiFunction<Integer, Integer, Integer> add = Integer::sum;
        check(add.apply(3, 4) == 7, "apply add");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
