/*
 * @test
 * @summary javax.xml.stream.events.StartElement conformance: interface hierarchy and methods.
 * @run main StartElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class StartElementTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.StartElement");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "has method getName");
        check(names.contains("getAttributes"), "has method getAttributes");
        check(names.contains("getNamespaces"), "has method getNamespaces");
        check(names.contains("getAttributeByName"), "has method getAttributeByName");
        check(names.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(names.contains("getNamespaceURI"), "has method getNamespaceURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
