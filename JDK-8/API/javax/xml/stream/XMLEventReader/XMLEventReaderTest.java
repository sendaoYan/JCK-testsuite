/*
 * @test
 * @summary javax.xml.stream.XMLEventReader conformance: interface hierarchy and methods.
 * @run main XMLEventReaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLEventReaderTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLEventReader");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.util.Iterator"), "implements java.util.Iterator");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("nextEvent"), "has method nextEvent");
        check(names.contains("hasNext"), "has method hasNext");
        check(names.contains("peek"), "has method peek");
        check(names.contains("getElementText"), "has method getElementText");
        check(names.contains("nextTag"), "has method nextTag");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("close"), "has method close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
