/*
 * @test
 * @summary com.sun.xml.internal.ws.wsdl.parser.WSDLConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLConstantsTest
 */

import java.lang.reflect.Modifier;


public class WSDLConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.wsdl.parser.WSDLConstants", false, WSDLConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.wsdl.parser.WSDLConstants"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ATTR_LOCATION"), "field ATTR_LOCATION");
        check(fn.contains("ATTR_NAME"), "field ATTR_NAME");
        check(fn.contains("ATTR_TNS"), "field ATTR_TNS");
        check(fn.contains("ATTR_TRANSPORT"), "field ATTR_TRANSPORT");
        check(fn.contains("NS_SOAP11_HTTP_BINDING"), "field NS_SOAP11_HTTP_BINDING");
        check(fn.contains("NS_SOAP12_BINDING"), "field NS_SOAP12_BINDING");
        check(fn.contains("NS_SOAP12_BINDING_ADDRESS"), "field NS_SOAP12_BINDING_ADDRESS");
        check(fn.contains("NS_SOAP_BINDING"), "field NS_SOAP_BINDING");
        check(fn.contains("NS_SOAP_BINDING_ADDRESS"), "field NS_SOAP_BINDING_ADDRESS");
        check(fn.contains("NS_WSDL"), "field NS_WSDL");
        check(fn.contains("NS_XMLNS"), "field NS_XMLNS");
        check(fn.contains("PREFIX_NS_WSDL"), "field PREFIX_NS_WSDL");
        check(fn.contains("QNAME_ADDRESS"), "field QNAME_ADDRESS");
        check(fn.contains("QNAME_BINDING"), "field QNAME_BINDING");
        check(fn.contains("QNAME_DEFINITIONS"), "field QNAME_DEFINITIONS");
        check(fn.contains("QNAME_DOCUMENTATION"), "field QNAME_DOCUMENTATION");
        check(fn.contains("QNAME_FAULT"), "field QNAME_FAULT");
        check(fn.contains("QNAME_IMPORT"), "field QNAME_IMPORT");
        check(fn.contains("QNAME_INPUT"), "field QNAME_INPUT");
        check(fn.contains("QNAME_MESSAGE"), "field QNAME_MESSAGE");
        check(fn.contains("QNAME_OPERATION"), "field QNAME_OPERATION");
        check(fn.contains("QNAME_OUTPUT"), "field QNAME_OUTPUT");
        check(fn.contains("QNAME_PART"), "field QNAME_PART");
        check(fn.contains("QNAME_PORT"), "field QNAME_PORT");
        check(fn.contains("QNAME_PORT_TYPE"), "field QNAME_PORT_TYPE");
        check(fn.contains("QNAME_SCHEMA"), "field QNAME_SCHEMA");
        check(fn.contains("QNAME_SERVICE"), "field QNAME_SERVICE");
        check(fn.contains("QNAME_TYPES"), "field QNAME_TYPES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
