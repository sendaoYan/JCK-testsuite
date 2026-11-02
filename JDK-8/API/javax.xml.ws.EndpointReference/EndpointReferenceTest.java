/*
 * @test
 * @summary javax.xml.ws.EndpointReference conformance: abstract class hierarchy and methods.
 * @run main EndpointReferenceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class EndpointReferenceTest {
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
        Class<?> c = Class.forName("javax.xml.ws.EndpointReference");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("readFrom"), "has method readFrom");
        check(names.contains("writeTo"), "has method writeTo");
        check(names.contains("getPort"), "has method getPort");
        check(names.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
