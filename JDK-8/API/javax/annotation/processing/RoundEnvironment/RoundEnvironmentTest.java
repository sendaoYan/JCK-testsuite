/*
 * @test
 * @summary javax.annotation.processing.RoundEnvironment conformance: interface hierarchy and methods.
 * @run main RoundEnvironmentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class RoundEnvironmentTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.RoundEnvironment");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("processingOver"), "has method processingOver");
        check(names.contains("errorRaised"), "has method errorRaised");
        check(names.contains("getRootElements"), "has method getRootElements");
        check(names.contains("getElementsAnnotatedWith"), "has method getElementsAnnotatedWith");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
