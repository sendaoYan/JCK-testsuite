/*
 * @test
 * @summary javax.xml.stream.events.StartDocument conformance: interface hierarchy and methods.
 * @run main StartDocumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class StartDocumentTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.StartDocument");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSystemId"), "has method getSystemId");
        check(names.contains("getCharacterEncodingScheme"), "has method getCharacterEncodingScheme");
        check(names.contains("encodingSet"), "has method encodingSet");
        check(names.contains("isStandalone"), "has method isStandalone");
        check(names.contains("standaloneSet"), "has method standaloneSet");
        check(names.contains("getVersion"), "has method getVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
