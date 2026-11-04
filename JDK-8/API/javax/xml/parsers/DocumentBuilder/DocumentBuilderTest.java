/*
 * @test
 * @summary javax.xml.parsers.DocumentBuilder conformance: abstract class hierarchy and methods.
 * @run main DocumentBuilderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DocumentBuilderTest {
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
        Class<?> c = Class.forName("javax.xml.parsers.DocumentBuilder");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("reset"), "has method reset");
        check(names.contains("parse"), "has method parse");
        check(names.contains("isNamespaceAware"), "has method isNamespaceAware");
        check(names.contains("isValidating"), "has method isValidating");
        check(names.contains("setEntityResolver"), "has method setEntityResolver");
        check(names.contains("setErrorHandler"), "has method setErrorHandler");
        check(names.contains("newDocument"), "has method newDocument");
        check(names.contains("getDOMImplementation"), "has method getDOMImplementation");
        check(names.contains("getSchema"), "has method getSchema");
        check(names.contains("isXIncludeAware"), "has method isXIncludeAware");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
