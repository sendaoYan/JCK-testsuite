/*
 * @test
 * @summary javax.xml.soap.SOAPEnvelope conformance: interface hierarchy and methods.
 * @run main SOAPEnvelopeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPEnvelopeTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPEnvelope");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createName"), "has method createName");
        check(names.contains("getHeader"), "has method getHeader");
        check(names.contains("getBody"), "has method getBody");
        check(names.contains("addHeader"), "has method addHeader");
        check(names.contains("addBody"), "has method addBody");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
