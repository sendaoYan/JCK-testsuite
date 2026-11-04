/*
 * @test
 * @summary com.sun.xml.internal.ws.transport.http.server.EndpointImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main EndpointImplTest
 */

import java.lang.reflect.Modifier;


public class EndpointImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.transport.http.server.EndpointImpl", false, EndpointImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.transport.http.server.EndpointImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.xml.ws.Endpoint"), "superclass javax.xml.ws.Endpoint");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getEndpointReference"), "method getEndpointReference");
        check(mn.contains("getExecutor"), "method getExecutor");
        check(mn.contains("getImplementor"), "method getImplementor");
        check(mn.contains("getMetadata"), "method getMetadata");
        check(mn.contains("getProperties"), "method getProperties");
        check(mn.contains("isPublished"), "method isPublished");
        check(mn.contains("publish"), "method publish");
        check(mn.contains("setEndpointContext"), "method setEndpointContext");
        check(mn.contains("setExecutor"), "method setExecutor");
        check(mn.contains("setMetadata"), "method setMetadata");
        check(mn.contains("setProperties"), "method setProperties");
        check(mn.contains("stop"), "method stop");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
