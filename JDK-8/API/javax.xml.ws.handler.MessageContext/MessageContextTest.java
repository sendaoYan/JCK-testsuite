/*
 * @test
 * @summary javax.xml.ws.handler.MessageContext conformance: interface hierarchy and methods.
 * @run main MessageContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class MessageContextTest {
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
        Class<?> c = Class.forName("javax.xml.ws.handler.MessageContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.util.Map"), "implements java.util.Map");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setScope"), "has method setScope");
        check(names.contains("getScope"), "has method getScope");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
