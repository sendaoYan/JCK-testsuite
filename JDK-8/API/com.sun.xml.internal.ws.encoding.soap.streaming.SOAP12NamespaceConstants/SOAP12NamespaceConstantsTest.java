/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.soap.streaming.SOAP12NamespaceConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SOAP12NamespaceConstantsTest
 */

import java.lang.reflect.Modifier;


public class SOAP12NamespaceConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.soap.streaming.SOAP12NamespaceConstants", false, SOAP12NamespaceConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.soap.streaming.SOAP12NamespaceConstants"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ACTOR_NEXT"), "field ACTOR_NEXT");
        check(fn.contains("ATTR_ACTOR"), "field ATTR_ACTOR");
        check(fn.contains("ATTR_ENCODING_STYLE"), "field ATTR_ENCODING_STYLE");
        check(fn.contains("ATTR_MISUNDERSTOOD"), "field ATTR_MISUNDERSTOOD");
        check(fn.contains("ATTR_MUST_UNDERSTAND"), "field ATTR_MUST_UNDERSTAND");
        check(fn.contains("ATTR_NOT_UNDERSTOOD_QNAME"), "field ATTR_NOT_UNDERSTOOD_QNAME");
        check(fn.contains("ENCODING"), "field ENCODING");
        check(fn.contains("ENVELOPE"), "field ENVELOPE");
        check(fn.contains("ROLE_NEXT"), "field ROLE_NEXT");
        check(fn.contains("ROLE_NONE"), "field ROLE_NONE");
        check(fn.contains("ROLE_ULTIMATE_RECEIVER"), "field ROLE_ULTIMATE_RECEIVER");
        check(fn.contains("SOAP_RPC"), "field SOAP_RPC");
        check(fn.contains("SOAP_UPGRADE"), "field SOAP_UPGRADE");
        check(fn.contains("TAG_BODY"), "field TAG_BODY");
        check(fn.contains("TAG_ENVELOPE"), "field TAG_ENVELOPE");
        check(fn.contains("TAG_HEADER"), "field TAG_HEADER");
        check(fn.contains("TAG_NOT_UNDERSTOOD"), "field TAG_NOT_UNDERSTOOD");
        check(fn.contains("TAG_RESULT"), "field TAG_RESULT");
        check(fn.contains("TRANSPORT_HTTP"), "field TRANSPORT_HTTP");
        check(fn.contains("XML_NS"), "field XML_NS");
        check(fn.contains("XSD"), "field XSD");
        check(fn.contains("XSI"), "field XSI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
