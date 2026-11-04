/*
 * @test
 * @summary javax.xml.xpath.XPathVariableResolver conformance: interface hierarchy and methods.
 * @run main XPathVariableResolverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XPathVariableResolverTest {
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
        Class<?> c = Class.forName("javax.xml.xpath.XPathVariableResolver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("resolveVariable"), "has method resolveVariable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
