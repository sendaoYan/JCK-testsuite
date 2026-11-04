/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.soap.SOAP12Constants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SOAP12ConstantsTest
 */

import java.lang.reflect.Modifier;


public class SOAP12ConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.soap.SOAP12Constants", false, SOAP12ConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.soap.SOAP12Constants"), "name");
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
        check(fn.contains("FAULT_CODE_MISUNDERSTOOD"), "field FAULT_CODE_MISUNDERSTOOD");
        check(fn.contains("FAULT_CODE_MUST_UNDERSTAND"), "field FAULT_CODE_MUST_UNDERSTAND");
        check(fn.contains("FAULT_CODE_PROCEDURE_NOT_PRESENT"), "field FAULT_CODE_PROCEDURE_NOT_PRESENT");
        check(fn.contains("FAULT_CODE_VERSION_MISMATCH"), "field FAULT_CODE_VERSION_MISMATCH");
        check(fn.contains("QNAME_ENVELOPE_ENCODINGSTYLE"), "field QNAME_ENVELOPE_ENCODINGSTYLE");
        check(fn.contains("QNAME_FAULT_CODE"), "field QNAME_FAULT_CODE");
        check(fn.contains("QNAME_FAULT_DETAIL"), "field QNAME_FAULT_DETAIL");
        check(fn.contains("QNAME_FAULT_NODE"), "field QNAME_FAULT_NODE");
        check(fn.contains("QNAME_FAULT_REASON"), "field QNAME_FAULT_REASON");
        check(fn.contains("QNAME_FAULT_REASON_TEXT"), "field QNAME_FAULT_REASON_TEXT");
        check(fn.contains("QNAME_FAULT_ROLE"), "field QNAME_FAULT_ROLE");
        check(fn.contains("QNAME_FAULT_SUBCODE"), "field QNAME_FAULT_SUBCODE");
        check(fn.contains("QNAME_FAULT_VALUE"), "field QNAME_FAULT_VALUE");
        check(fn.contains("QNAME_MUSTUNDERSTAND"), "field QNAME_MUSTUNDERSTAND");
        check(fn.contains("QNAME_NOT_UNDERSTOOD"), "field QNAME_NOT_UNDERSTOOD");
        check(fn.contains("QNAME_ROLE"), "field QNAME_ROLE");
        check(fn.contains("QNAME_SOAP_BODY"), "field QNAME_SOAP_BODY");
        check(fn.contains("QNAME_SOAP_ENVELOPE"), "field QNAME_SOAP_ENVELOPE");
        check(fn.contains("QNAME_SOAP_FAULT"), "field QNAME_SOAP_FAULT");
        check(fn.contains("QNAME_SOAP_HEADER"), "field QNAME_SOAP_HEADER");
        check(fn.contains("QNAME_SOAP_RESULT"), "field QNAME_SOAP_RESULT");
        check(fn.contains("QNAME_SOAP_RPC"), "field QNAME_SOAP_RPC");
        check(fn.contains("QNAME_UPGRADE"), "field QNAME_UPGRADE");
        check(fn.contains("QNAME_UPGRADE_SUPPORTED_ENVELOPE"), "field QNAME_UPGRADE_SUPPORTED_ENVELOPE");
        check(fn.contains("URI_ENCODING"), "field URI_ENCODING");
        check(fn.contains("URI_ENVELOPE"), "field URI_ENVELOPE");
        check(fn.contains("URI_HTTP"), "field URI_HTTP");
        check(fn.contains("URI_SOAP_RPC"), "field URI_SOAP_RPC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
