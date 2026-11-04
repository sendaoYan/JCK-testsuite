/*
 * @test
 * @summary com.sun.xml.internal.ws.api.addressing.WSEndpointReference.Metadata conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MetadataTest
 */

import java.lang.reflect.Modifier;


public class MetadataTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.addressing.WSEndpointReference$Metadata", false, MetadataTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.addressing.WSEndpointReference$Metadata"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("getPortTypeName"), "method getPortTypeName");
        check(mn.contains("getServiceName"), "method getServiceName");
        check(mn.contains("getWsdlSource"), "method getWsdlSource");
        check(mn.contains("getWsdliLocation"), "method getWsdliLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
