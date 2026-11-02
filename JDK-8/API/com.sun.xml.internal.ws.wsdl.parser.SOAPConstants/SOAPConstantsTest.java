/*
 * @test
 * @summary com.sun.xml.internal.ws.wsdl.parser.SOAPConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SOAPConstantsTest
 */

import java.lang.reflect.Modifier;


public class SOAPConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.wsdl.parser.SOAPConstants", false, SOAPConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.wsdl.parser.SOAPConstants"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("NS_SOAP_ENCODING"), "field NS_SOAP_ENCODING");
        check(fn.contains("NS_WSDL_SOAP"), "field NS_WSDL_SOAP");
        check(fn.contains("NS_WSDL_SOAP12"), "field NS_WSDL_SOAP12");
        check(fn.contains("QNAME_ADDRESS"), "field QNAME_ADDRESS");
        check(fn.contains("QNAME_BINDING"), "field QNAME_BINDING");
        check(fn.contains("QNAME_BODY"), "field QNAME_BODY");
        check(fn.contains("QNAME_FAULT"), "field QNAME_FAULT");
        check(fn.contains("QNAME_HEADER"), "field QNAME_HEADER");
        check(fn.contains("QNAME_HEADERFAULT"), "field QNAME_HEADERFAULT");
        check(fn.contains("QNAME_MUSTUNDERSTAND"), "field QNAME_MUSTUNDERSTAND");
        check(fn.contains("QNAME_OPERATION"), "field QNAME_OPERATION");
        check(fn.contains("QNAME_SOAP12ADDRESS"), "field QNAME_SOAP12ADDRESS");
        check(fn.contains("QNAME_SOAP12BODY"), "field QNAME_SOAP12BODY");
        check(fn.contains("QNAME_SOAP12HEADER"), "field QNAME_SOAP12HEADER");
        check(fn.contains("QNAME_SOAP12OPERATION"), "field QNAME_SOAP12OPERATION");
        check(fn.contains("URI_ENVELOPE"), "field URI_ENVELOPE");
        check(fn.contains("URI_ENVELOPE12"), "field URI_ENVELOPE12");
        check(fn.contains("URI_SOAP_TRANSPORT_HTTP"), "field URI_SOAP_TRANSPORT_HTTP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
