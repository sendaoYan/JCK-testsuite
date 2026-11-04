/*
 * @test
 * @summary java.util.function.IntToDoubleFunction conformance: white-box structure + black-box behavior.
 * @run main IntToDoubleFunctionTest
 */

import java.util.Comparator;
import java.util.function.*;

public class IntToDoubleFunctionTest {
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
        Class<?> c = cls("java.util.function.IntToDoubleFunction");
        check(c.isInterface(), "IntToDoubleFunction is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "applyAsDouble", int.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        IntToDoubleFunction f = i -> i / 2.0;
        check(f.applyAsDouble(5) == 2.5, "applyAsDouble");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
