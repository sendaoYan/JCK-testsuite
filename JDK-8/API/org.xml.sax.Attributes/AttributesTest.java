/*
 * @test
 * @summary org.xml.sax.Attributes conformance: interface hierarchy and methods.
 * @run main AttributesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttributesTest {
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
        Class<?> c = Class.forName("org.xml.sax.Attributes");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLength"), "has method getLength");
        check(names.contains("getURI"), "has method getURI");
        check(names.contains("getLocalName"), "has method getLocalName");
        check(names.contains("getQName"), "has method getQName");
        check(names.contains("getType"), "has method getType");
        check(names.contains("getValue"), "has method getValue");
        check(names.contains("getIndex"), "has method getIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
