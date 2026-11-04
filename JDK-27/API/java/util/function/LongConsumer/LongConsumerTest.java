/*
 * @test
 * @summary java.util.function.LongConsumer conformance: white-box structure + black-box behavior.
 * @run main LongConsumerTest
 */

import java.util.Comparator;
import java.util.function.*;

public class LongConsumerTest {
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
        Class<?> c = cls("java.util.function.LongConsumer");
        check(c.isInterface(), "LongConsumer is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "accept", long.class);
        hasMethod(c, "andThen", LongConsumer.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        long[] acc = {0L};
        LongConsumer add = v -> acc[0] += v;
        add.accept(5L);
        check(acc[0] == 5L, "accept");
        LongConsumer mul = v -> acc[0] *= v;
        add.andThen(mul).accept(3L);
        check(acc[0] == 24L, "andThen runs in order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
