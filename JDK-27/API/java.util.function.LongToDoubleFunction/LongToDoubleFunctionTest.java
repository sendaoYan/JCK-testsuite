/*
 * @test
 * @summary java.util.function.LongToDoubleFunction conformance: white-box structure + black-box behavior.
 * @run main LongToDoubleFunctionTest
 */

import java.util.Comparator;
import java.util.function.*;

public class LongToDoubleFunctionTest {
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
        Class<?> c = cls("java.util.function.LongToDoubleFunction");
        check(c.isInterface(), "LongToDoubleFunction is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "applyAsDouble", long.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LongToDoubleFunction f = l -> l / 2.0;
        check(f.applyAsDouble(5L) == 2.5, "applyAsDouble");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
