/*
 * @test
 * @summary com.sun.xml.internal.ws.server.EndpointFactory conformance: reflection (kind, superclass, interfaces, methods).
 * @run main EndpointFactoryTest
 */

import java.lang.reflect.Modifier;


public class EndpointFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.server.EndpointFactory", false, EndpointFactoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.server.EndpointFactory"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("create"), "method create");
        check(mn.contains("createEndpoint"), "method createEndpoint");
        check(mn.contains("createProviderInvokerTube"), "method createProviderInvokerTube");
        check(mn.contains("createSEIInvokerTube"), "method createSEIInvokerTube");
        check(mn.contains("getDefaultPortName"), "method getDefaultPortName");
        check(mn.contains("getDefaultServiceName"), "method getDefaultServiceName");
        check(mn.contains("getExternalMetadatReader"), "method getExternalMetadatReader");
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("getWsdlLocation"), "method getWsdlLocation");
        check(mn.contains("isUseProviderTube"), "method isUseProviderTube");
        check(mn.contains("verifyImplementorClass"), "method verifyImplementorClass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
