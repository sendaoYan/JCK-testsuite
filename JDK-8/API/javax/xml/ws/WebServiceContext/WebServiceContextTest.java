/*
 * @test
 * @summary javax.xml.ws.WebServiceContext conformance: interface hierarchy and methods.
 * @run main WebServiceContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class WebServiceContextTest {
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
        Class<?> c = Class.forName("javax.xml.ws.WebServiceContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMessageContext"), "has method getMessageContext");
        check(names.contains("getUserPrincipal"), "has method getUserPrincipal");
        check(names.contains("isUserInRole"), "has method isUserInRole");
        check(names.contains("getEndpointReference"), "has method getEndpointReference");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
