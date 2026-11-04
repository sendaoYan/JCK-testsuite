/*
 * @test
 * @summary com.sun.xml.internal.ws.addressing.W3CAddressingMetadataConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main W3CAddressingMetadataConstantsTest
 */

import java.lang.reflect.Modifier;


public class W3CAddressingMetadataConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.addressing.W3CAddressingMetadataConstants", false, W3CAddressingMetadataConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.addressing.W3CAddressingMetadataConstants"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("WSAM_ACTION_QNAME"), "field WSAM_ACTION_QNAME");
        check(fn.contains("WSAM_ADDRESSING_ASSERTION"), "field WSAM_ADDRESSING_ASSERTION");
        check(fn.contains("WSAM_ADDRESSING_ASSERTION_NAME"), "field WSAM_ADDRESSING_ASSERTION_NAME");
        check(fn.contains("WSAM_ANONYMOUS_NESTED_ASSERTION"), "field WSAM_ANONYMOUS_NESTED_ASSERTION");
        check(fn.contains("WSAM_ANONYMOUS_NESTED_ASSERTION_NAME"), "field WSAM_ANONYMOUS_NESTED_ASSERTION_NAME");
        check(fn.contains("WSAM_NAMESPACE_NAME"), "field WSAM_NAMESPACE_NAME");
        check(fn.contains("WSAM_NONANONYMOUS_NESTED_ASSERTION"), "field WSAM_NONANONYMOUS_NESTED_ASSERTION");
        check(fn.contains("WSAM_NONANONYMOUS_NESTED_ASSERTION_NAME"), "field WSAM_NONANONYMOUS_NESTED_ASSERTION_NAME");
        check(fn.contains("WSAM_PREFIX_NAME"), "field WSAM_PREFIX_NAME");
        check(fn.contains("WSAM_WSDLI_ATTRIBUTE_LOCALNAME"), "field WSAM_WSDLI_ATTRIBUTE_LOCALNAME");
        check(fn.contains("WSAM_WSDLI_ATTRIBUTE_NAMESPACE"), "field WSAM_WSDLI_ATTRIBUTE_NAMESPACE");
        check(fn.contains("WSAM_WSDLI_ATTRIBUTE_PREFIX"), "field WSAM_WSDLI_ATTRIBUTE_PREFIX");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
