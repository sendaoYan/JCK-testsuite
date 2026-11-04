/*
 * @test
 * @summary org.xml.sax.AttributeList conformance: interface hierarchy and methods.
 * @run main AttributeListTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttributeListTest {
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
        Class<?> c = Class.forName("org.xml.sax.AttributeList");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLength"), "has method getLength");
        check(names.contains("getName"), "has method getName");
        check(names.contains("getType"), "has method getType");
        check(names.contains("getValue"), "has method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
