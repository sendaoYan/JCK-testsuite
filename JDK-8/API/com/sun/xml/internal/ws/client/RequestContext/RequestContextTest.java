/*
 * @test
 * @summary com.sun.xml.internal.ws.client.RequestContext conformance: reflection (kind, superclass, interfaces, methods).
 * @run main RequestContextTest
 */

import java.lang.reflect.Modifier;


public class RequestContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.RequestContext", false, RequestContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.RequestContext"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.message.BaseDistributedPropertySet"), "superclass com.oracle.webservices.internal.api.message.BaseDistributedPropertySet");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addSatellite"), "method addSatellite");
        check(mn.contains("copy"), "method copy");
        check(mn.contains("fill"), "method fill");
        check(mn.contains("get"), "method get");
        check(mn.contains("getContentNegotiationString"), "method getContentNegotiationString");
        check(mn.contains("getEndPointAddressString"), "method getEndPointAddressString");
        check(mn.contains("getEndpointAddress"), "method getEndpointAddress");
        check(mn.contains("getPropertyMap"), "method getPropertyMap");
        check(mn.contains("getSoapAction"), "method getSoapAction");
        check(mn.contains("getSoapActionUse"), "method getSoapActionUse");
        check(mn.contains("mapAllowsAdditionalProperties"), "method mapAllowsAdditionalProperties");
        check(mn.contains("put"), "method put");
        check(mn.contains("setContentNegotiationString"), "method setContentNegotiationString");
        check(mn.contains("setEndPointAddressString"), "method setEndPointAddressString");
        check(mn.contains("setEndpointAddress"), "method setEndpointAddress");
        check(mn.contains("setSoapAction"), "method setSoapAction");
        check(mn.contains("setSoapActionUse"), "method setSoapActionUse");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("contentNegotiation"), "field contentNegotiation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
