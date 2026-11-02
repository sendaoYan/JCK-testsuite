/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.soap.SOAPConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SOAPConstantsTest
 */

import java.lang.reflect.Modifier;


public class SOAPConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.soap.SOAPConstants", false, SOAPConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.soap.SOAPConstants"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("FAULT_CODE_BAD_ARGUMENTS"), "field FAULT_CODE_BAD_ARGUMENTS");
        check(fn.contains("FAULT_CODE_DATA_ENCODING_UNKNOWN"), "field FAULT_CODE_DATA_ENCODING_UNKNOWN");
        check(fn.contains("FAULT_CODE_MUST_UNDERSTAND"), "field FAULT_CODE_MUST_UNDERSTAND");
        check(fn.contains("FAULT_CODE_PROCEDURE_NOT_PRESENT"), "field FAULT_CODE_PROCEDURE_NOT_PRESENT");
        check(fn.contains("FAULT_CODE_VERSION_MISMATCH"), "field FAULT_CODE_VERSION_MISMATCH");
        check(fn.contains("NS_WSDL_SOAP"), "field NS_WSDL_SOAP");
        check(fn.contains("QNAME_ENVELOPE_ENCODINGSTYLE"), "field QNAME_ENVELOPE_ENCODINGSTYLE");
        check(fn.contains("QNAME_MUSTUNDERSTAND"), "field QNAME_MUSTUNDERSTAND");
        check(fn.contains("QNAME_ROLE"), "field QNAME_ROLE");
        check(fn.contains("QNAME_SOAP_BODY"), "field QNAME_SOAP_BODY");
        check(fn.contains("QNAME_SOAP_ENVELOPE"), "field QNAME_SOAP_ENVELOPE");
        check(fn.contains("QNAME_SOAP_FAULT"), "field QNAME_SOAP_FAULT");
        check(fn.contains("QNAME_SOAP_FAULT_ACTOR"), "field QNAME_SOAP_FAULT_ACTOR");
        check(fn.contains("QNAME_SOAP_FAULT_CODE"), "field QNAME_SOAP_FAULT_CODE");
        check(fn.contains("QNAME_SOAP_FAULT_DETAIL"), "field QNAME_SOAP_FAULT_DETAIL");
        check(fn.contains("QNAME_SOAP_FAULT_STRING"), "field QNAME_SOAP_FAULT_STRING");
        check(fn.contains("QNAME_SOAP_HEADER"), "field QNAME_SOAP_HEADER");
        check(fn.contains("URI_ENCODING"), "field URI_ENCODING");
        check(fn.contains("URI_ENVELOPE"), "field URI_ENVELOPE");
        check(fn.contains("URI_HTTP"), "field URI_HTTP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
