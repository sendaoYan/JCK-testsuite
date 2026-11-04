/*
 * @test
 * @summary javax.xml.transform.dom.DOMLocator conformance: interface hierarchy and methods.
 * @run main DOMLocatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DOMLocatorTest {
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
        Class<?> c = Class.forName("javax.xml.transform.dom.DOMLocator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.transform.SourceLocator"), "implements javax.xml.transform.SourceLocator");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOriginatingNode"), "has method getOriginatingNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
