/*
 * @test
 * @summary java.util.function.Supplier conformance: white-box structure + black-box behavior.
 * @run main SupplierTest
 */

import java.util.Comparator;
import java.util.function.*;

public class SupplierTest {
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
        Class<?> c = cls("java.util.function.Supplier");
        check(c.isInterface(), "Supplier is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "get");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Supplier<Integer> fortyTwo = () -> 42;
        check(fortyTwo.get() == 42, "get");
        Supplier<String> s = () -> "x";
        check("x".equals(s.get()), "get string");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
