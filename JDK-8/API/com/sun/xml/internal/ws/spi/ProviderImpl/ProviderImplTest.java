/*
 * @test
 * @summary com.sun.xml.internal.ws.spi.ProviderImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ProviderImplTest
 */

import java.lang.reflect.Modifier;


public class ProviderImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.spi.ProviderImpl", false, ProviderImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.spi.ProviderImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.xml.ws.spi.Provider"), "superclass javax.xml.ws.spi.Provider");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createAndPublishEndpoint"), "method createAndPublishEndpoint");
        check(mn.contains("createEndpoint"), "method createEndpoint");
        check(mn.contains("createServiceDelegate"), "method createServiceDelegate");
        check(mn.contains("createW3CEndpointReference"), "method createW3CEndpointReference");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("readEndpointReference"), "method readEndpointReference");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("INSTANCE"), "field INSTANCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
