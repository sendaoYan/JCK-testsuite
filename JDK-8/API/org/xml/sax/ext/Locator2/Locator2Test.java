/*
 * @test
 * @summary org.xml.sax.ext.Locator2 conformance: interface hierarchy and methods.
 * @run main Locator2Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class Locator2Test {
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
        Class<?> c = Class.forName("org.xml.sax.ext.Locator2");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.Locator"), "implements org.xml.sax.Locator");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getXMLVersion"), "has method getXMLVersion");
        check(names.contains("getEncoding"), "has method getEncoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
