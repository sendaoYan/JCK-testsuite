/*
 * @test
 * @summary java.util.function.Function conformance: white-box structure + black-box behavior.
 * @run main FunctionTest
 */

import java.util.Comparator;
import java.util.function.*;

public class FunctionTest {
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
        Class<?> c = cls("java.util.function.Function");
        check(c.isInterface(), "Function is an interface");
        check(c.isAnnotationPresent(java.lang.FunctionalInterface.class), "@FunctionalInterface");
        hasMethod(c, "apply", Object.class);
        hasMethod(c, "andThen", Function.class);
        hasMethod(c, "compose", Function.class);
        hasMethod(c, "identity");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Function<String, Integer> len = String::length;
        check(len.apply("abc") == 3, "apply");
        Function<Integer, Integer> dbl = x -> x * 2;
        check(len.andThen(dbl).apply("abc") == 6, "andThen applies f then g");
        Function<Integer, String> str = Object::toString;
        check(len.compose(str).apply(12345) == 5, "compose applies g then f");
        check(Function.identity().apply("x").equals("x"), "identity passes value through");
        check(Function.<String>identity().apply(null) == null, "identity passes null through");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
