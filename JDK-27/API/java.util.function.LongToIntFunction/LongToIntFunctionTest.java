/*
 * @test
 * @summary java.util.function.LongToIntFunction conformance: white-box structure + black-box behavior.
 * @run main LongToIntFunctionTest
 */

import java.util.Comparator;
import java.util.function.*;

public class LongToIntFunctionTest {
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
        Class<?> c = cls("java.util.function.LongToIntFunction");
        check(c.isInterface(), "LongToIntFunction is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "applyAsInt", long.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LongToIntFunction f = l -> (int) l;
        check(f.applyAsInt(7L) == 7, "applyAsInt");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
