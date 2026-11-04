/*
 * @test
 * @summary javax.xml.ws.Endpoint conformance: abstract class hierarchy and methods.
 * @run main EndpointTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class EndpointTest {
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
        Class<?> c = Class.forName("javax.xml.ws.Endpoint");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("create"), "has method create");
        check(names.contains("getBinding"), "has method getBinding");
        check(names.contains("getImplementor"), "has method getImplementor");
        check(names.contains("publish"), "has method publish");
        check(names.contains("stop"), "has method stop");
        check(names.contains("isPublished"), "has method isPublished");
        check(names.contains("getMetadata"), "has method getMetadata");
        check(names.contains("setMetadata"), "has method setMetadata");
        check(names.contains("getExecutor"), "has method getExecutor");
        check(names.contains("setExecutor"), "has method setExecutor");
        check(names.contains("getProperties"), "has method getProperties");
        check(names.contains("setProperties"), "has method setProperties");
        check(names.contains("getEndpointReference"), "has method getEndpointReference");
        check(names.contains("setEndpointContext"), "has method setEndpointContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
