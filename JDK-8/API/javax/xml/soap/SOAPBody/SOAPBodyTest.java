/*
 * @test
 * @summary javax.xml.soap.SOAPBody conformance: interface hierarchy and methods.
 * @run main SOAPBodyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPBodyTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPBody");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addFault"), "has method addFault");
        check(names.contains("hasFault"), "has method hasFault");
        check(names.contains("getFault"), "has method getFault");
        check(names.contains("addBodyElement"), "has method addBodyElement");
        check(names.contains("addDocument"), "has method addDocument");
        check(names.contains("extractContentAsDocument"), "has method extractContentAsDocument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
