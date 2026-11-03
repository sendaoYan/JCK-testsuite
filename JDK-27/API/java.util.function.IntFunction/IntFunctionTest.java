/*
 * @test
 * @summary java.util.function.IntFunction conformance: white-box structure + black-box behavior.
 * @run main IntFunctionTest
 */

import java.util.Comparator;
import java.util.function.*;

public class IntFunctionTest {
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
        Class<?> c = cls("java.util.function.IntFunction");
        check(c.isInterface(), "IntFunction is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "apply", int.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        IntFunction<String> f = i -> "v" + i;
        check(f.apply(3).equals("v3"), "apply");
        IntFunction<java.util.List<String>> list = java.util.ArrayList::new;
        java.util.List<String> made = list.apply(2);
        check(made instanceof java.util.List && made.isEmpty(), "apply ArrayList::new");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
