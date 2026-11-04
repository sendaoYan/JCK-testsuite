/*
 * @test
 * @summary java.util.function.ObjIntConsumer conformance: white-box structure + black-box behavior.
 * @run main ObjIntConsumerTest
 */

import java.util.Comparator;
import java.util.function.*;

public class ObjIntConsumerTest {
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
        Class<?> c = cls("java.util.function.ObjIntConsumer");
        check(c.isInterface(), "ObjIntConsumer is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "accept", Object.class, int.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.util.List<String> acc = new java.util.ArrayList<>();
        ObjIntConsumer<String> f = (s, i) -> acc.add(s + i);
        f.accept("n", 5);
        check(acc.get(0).equals("n5"), "accept");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
