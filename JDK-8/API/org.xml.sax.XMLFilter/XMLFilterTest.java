/*
 * @test
 * @summary org.xml.sax.XMLFilter conformance: interface hierarchy and methods.
 * @run main XMLFilterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLFilterTest {
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
        Class<?> c = Class.forName("org.xml.sax.XMLFilter");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.XMLReader"), "implements org.xml.sax.XMLReader");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setParent"), "has method setParent");
        check(names.contains("getParent"), "has method getParent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
