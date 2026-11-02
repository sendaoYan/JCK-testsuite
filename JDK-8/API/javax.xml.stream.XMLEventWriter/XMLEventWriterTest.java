/*
 * @test
 * @summary javax.xml.stream.XMLEventWriter conformance: interface hierarchy and methods.
 * @run main XMLEventWriterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLEventWriterTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLEventWriter");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.util.XMLEventConsumer"), "implements javax.xml.stream.util.XMLEventConsumer");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("flush"), "has method flush");
        check(names.contains("close"), "has method close");
        check(names.contains("add"), "has method add");
        check(names.contains("getPrefix"), "has method getPrefix");
        check(names.contains("setPrefix"), "has method setPrefix");
        check(names.contains("setDefaultNamespace"), "has method setDefaultNamespace");
        check(names.contains("setNamespaceContext"), "has method setNamespaceContext");
        check(names.contains("getNamespaceContext"), "has method getNamespaceContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
