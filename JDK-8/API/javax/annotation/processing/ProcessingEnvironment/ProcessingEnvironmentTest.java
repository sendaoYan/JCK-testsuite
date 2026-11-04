/*
 * @test
 * @summary javax.annotation.processing.ProcessingEnvironment conformance: interface hierarchy and methods.
 * @run main ProcessingEnvironmentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ProcessingEnvironmentTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.ProcessingEnvironment");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOptions"), "has method getOptions");
        check(names.contains("getMessager"), "has method getMessager");
        check(names.contains("getFiler"), "has method getFiler");
        check(names.contains("getElementUtils"), "has method getElementUtils");
        check(names.contains("getTypeUtils"), "has method getTypeUtils");
        check(names.contains("getSourceVersion"), "has method getSourceVersion");
        check(names.contains("getLocale"), "has method getLocale");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
