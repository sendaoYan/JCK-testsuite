/*
 * @test
 * @summary javax.xml.bind.ValidationEventLocator conformance: interface hierarchy and methods.
 * @run main ValidationEventLocatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ValidationEventLocatorTest {
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
        Class<?> c = Class.forName("javax.xml.bind.ValidationEventLocator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getURL"), "has method getURL");
        check(names.contains("getOffset"), "has method getOffset");
        check(names.contains("getLineNumber"), "has method getLineNumber");
        check(names.contains("getColumnNumber"), "has method getColumnNumber");
        check(names.contains("getObject"), "has method getObject");
        check(names.contains("getNode"), "has method getNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
