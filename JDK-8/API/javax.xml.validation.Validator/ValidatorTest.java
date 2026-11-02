/*
 * @test
 * @summary javax.xml.validation.Validator conformance: abstract class hierarchy and methods.
 * @run main ValidatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ValidatorTest {
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
        Class<?> c = Class.forName("javax.xml.validation.Validator");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("reset"), "has method reset");
        check(names.contains("validate"), "has method validate");
        check(names.contains("setErrorHandler"), "has method setErrorHandler");
        check(names.contains("getErrorHandler"), "has method getErrorHandler");
        check(names.contains("setResourceResolver"), "has method setResourceResolver");
        check(names.contains("getResourceResolver"), "has method getResourceResolver");
        check(names.contains("getFeature"), "has method getFeature");
        check(names.contains("setFeature"), "has method setFeature");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
