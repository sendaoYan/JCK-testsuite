/*
 * @test
 * @summary java.util.function.Consumer conformance: white-box structure + black-box behavior.
 * @run main ConsumerTest
 */

import java.util.Comparator;
import java.util.function.*;

public class ConsumerTest {
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
        Class<?> c = cls("java.util.function.Consumer");
        check(c.isInterface(), "Consumer is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "accept", Object.class);
        hasMethod(c, "andThen", Consumer.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.util.List<String> acc = new java.util.ArrayList<>();
        Consumer<String> add = acc::add;
        Consumer<String> upper = s -> acc.add(s.toUpperCase());
        add.accept("a");
        check(acc.equals(java.util.Arrays.asList("a")), "accept");
        add.andThen(upper).accept("b");
        check(acc.size() == 3 && acc.get(1).equals("b") && acc.get(2).equals("B"),
              "andThen runs both consumers in order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
