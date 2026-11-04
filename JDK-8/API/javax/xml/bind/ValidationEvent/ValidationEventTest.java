/*
 * @test
 * @summary javax.xml.bind.ValidationEvent conformance: interface hierarchy and methods.
 * @run main ValidationEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ValidationEventTest {
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
        Class<?> c = Class.forName("javax.xml.bind.ValidationEvent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSeverity"), "has method getSeverity");
        check(names.contains("getMessage"), "has method getMessage");
        check(names.contains("getLinkedException"), "has method getLinkedException");
        check(names.contains("getLocator"), "has method getLocator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
