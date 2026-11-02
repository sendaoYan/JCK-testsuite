/*
 * @test
 * @summary com.sun.xml.internal.ws.developer.MemberSubmissionEndpointReference conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MemberSubmissionEndpointReferenceTest
 */

import java.lang.reflect.Modifier;


public class MemberSubmissionEndpointReferenceTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.developer.MemberSubmissionEndpointReference", false, MemberSubmissionEndpointReferenceTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.developer.MemberSubmissionEndpointReference"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("javax.xml.ws.EndpointReference"), "superclass javax.xml.ws.EndpointReference");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.addressing.v200408.MemberSubmissionAddressingConstants"), "implements com.sun.xml.internal.ws.addressing.v200408.MemberSubmissionAddressingConstants");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("toWSDLSource"), "method toWSDLSource");
        check(mn.contains("writeTo"), "method writeTo");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("MSNS"), "field MSNS");
        check(fn.contains("addr"), "field addr");
        check(fn.contains("attributes"), "field attributes");
        check(fn.contains("elements"), "field elements");
        check(fn.contains("portTypeName"), "field portTypeName");
        check(fn.contains("referenceParameters"), "field referenceParameters");
        check(fn.contains("referenceProperties"), "field referenceProperties");
        check(fn.contains("serviceName"), "field serviceName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
