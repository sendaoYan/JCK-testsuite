/*
 * @test
 * @summary java.util.function.IntConsumer conformance: white-box structure + black-box behavior.
 * @run main IntConsumerTest
 */

import java.util.Comparator;
import java.util.function.*;

public class IntConsumerTest {
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
        Class<?> c = cls("java.util.function.IntConsumer");
        check(c.isInterface(), "IntConsumer is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "accept", int.class);
        hasMethod(c, "andThen", IntConsumer.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        int[] acc = {0};
        IntConsumer add = v -> acc[0] += v;
        add.accept(5);
        check(acc[0] == 5, "accept");
        IntConsumer mul = v -> acc[0] *= v;
        add.andThen(mul).accept(3);
        check(acc[0] == 24, "andThen runs in order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
