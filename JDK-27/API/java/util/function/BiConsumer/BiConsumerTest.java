/*
 * @test
 * @summary java.util.function.BiConsumer conformance: white-box structure + black-box behavior.
 * @run main BiConsumerTest
 */

import java.util.Comparator;
import java.util.function.*;

public class BiConsumerTest {
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
        Class<?> c = cls("java.util.function.BiConsumer");
        check(c.isInterface(), "BiConsumer is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "accept", Object.class, Object.class);
        hasMethod(c, "andThen", BiConsumer.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.util.List<String> acc = new java.util.ArrayList<>();
        BiConsumer<String, Integer> pair = (s, i) -> acc.add(s + i);
        pair.accept("n", 7);
        check(acc.get(0).equals("n7"), "accept");
        BiConsumer<String, Integer> upper = (s, i) -> acc.add(s.toUpperCase());
        pair.andThen(upper).accept("a", 1);
        check(acc.size() == 3 && acc.get(1).equals("a1") && acc.get(2).equals("A"),
              "andThen runs both in order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
