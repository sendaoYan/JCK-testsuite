/*
 * @test
 * @summary java.util.function.ObjDoubleConsumer conformance: white-box structure + black-box behavior.
 * @run main ObjDoubleConsumerTest
 */

import java.util.Comparator;
import java.util.function.*;

public class ObjDoubleConsumerTest {
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
        Class<?> c = cls("java.util.function.ObjDoubleConsumer");
        check(c.isInterface(), "ObjDoubleConsumer is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "accept", Object.class, double.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.util.List<String> acc = new java.util.ArrayList<>();
        ObjDoubleConsumer<String> f = (s, d) -> acc.add(s + (int) d);
        f.accept("n", 5.0);
        check(acc.get(0).equals("n5"), "accept");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
