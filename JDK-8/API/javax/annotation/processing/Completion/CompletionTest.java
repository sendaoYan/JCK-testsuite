/*
 * @test
 * @summary javax.annotation.processing.Completion conformance: interface hierarchy and methods.
 * @run main CompletionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class CompletionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.Completion");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getValue"), "has method getValue");
        check(names.contains("getMessage"), "has method getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
