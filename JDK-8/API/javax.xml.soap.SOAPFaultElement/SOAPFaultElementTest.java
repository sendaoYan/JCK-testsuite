/*
 * @test
 * @summary javax.xml.soap.SOAPFaultElement conformance: interface hierarchy and methods.
 * @run main SOAPFaultElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPFaultElementTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPFaultElement");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
