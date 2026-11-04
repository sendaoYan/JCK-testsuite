/*
 * @test
 * @summary javax.xml.ws.handler.soap.SOAPMessageContext conformance: interface hierarchy and methods.
 * @run main SOAPMessageContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPMessageContextTest {
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
        Class<?> c = Class.forName("javax.xml.ws.handler.soap.SOAPMessageContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.handler.MessageContext"), "implements javax.xml.ws.handler.MessageContext");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMessage"), "has method getMessage");
        check(names.contains("setMessage"), "has method setMessage");
        check(names.contains("getHeaders"), "has method getHeaders");
        check(names.contains("getRoles"), "has method getRoles");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
