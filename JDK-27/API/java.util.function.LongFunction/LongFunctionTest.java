/*
 * @test
 * @summary java.util.function.LongFunction conformance: white-box structure + black-box behavior.
 * @run main LongFunctionTest
 */

import java.util.Comparator;
import java.util.function.*;

public class LongFunctionTest {
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
        Class<?> c = cls("java.util.function.LongFunction");
        check(c.isInterface(), "LongFunction is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "apply", long.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LongFunction<String> f = l -> "n" + l;
        check(f.apply(42L).equals("n42"), "apply");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
