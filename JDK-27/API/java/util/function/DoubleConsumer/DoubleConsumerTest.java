/*
 * @test
 * @summary java.util.function.DoubleConsumer conformance: white-box structure + black-box behavior.
 * @run main DoubleConsumerTest
 */

import java.util.Comparator;
import java.util.function.*;

public class DoubleConsumerTest {
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
        Class<?> c = cls("java.util.function.DoubleConsumer");
        check(c.isInterface(), "DoubleConsumer is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "accept", double.class);
        hasMethod(c, "andThen", DoubleConsumer.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        double[] acc = {0.0};
        DoubleConsumer add = v -> acc[0] += v;
        add.accept(5.0);
        check(acc[0] == 5.0, "accept");
        DoubleConsumer dbl = v -> acc[0] *= v;
        add.andThen(dbl).accept(3.0);
        check(acc[0] == 24.0, "andThen runs in order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
