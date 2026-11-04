/*
 * @test
 * @summary javax.xml.ws.soap.SOAPBinding conformance: interface hierarchy and methods.
 * @run main SOAPBindingTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPBindingTest {
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
        Class<?> c = Class.forName("javax.xml.ws.soap.SOAPBinding");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.Binding"), "implements javax.xml.ws.Binding");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRoles"), "has method getRoles");
        check(names.contains("setRoles"), "has method setRoles");
        check(names.contains("isMTOMEnabled"), "has method isMTOMEnabled");
        check(names.contains("setMTOMEnabled"), "has method setMTOMEnabled");
        check(names.contains("getSOAPFactory"), "has method getSOAPFactory");
        check(names.contains("getMessageFactory"), "has method getMessageFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
