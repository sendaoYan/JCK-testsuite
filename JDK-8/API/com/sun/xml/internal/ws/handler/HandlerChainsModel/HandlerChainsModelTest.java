/*
 * @test
 * @summary com.sun.xml.internal.ws.handler.HandlerChainsModel conformance: reflection (kind, superclass, interfaces, methods).
 * @run main HandlerChainsModelTest
 */

import java.lang.reflect.Modifier;


public class HandlerChainsModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.handler.HandlerChainsModel", false, HandlerChainsModelTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.handler.HandlerChainsModel"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getHandlersForPortInfo"), "method getHandlersForPortInfo");
        check(mn.contains("getId"), "method getId");
        check(mn.contains("parseHandlerConfigFile"), "method parseHandlerConfigFile");
        check(mn.contains("parseHandlerFile"), "method parseHandlerFile");
        check(mn.contains("setId"), "method setId");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("NS_109"), "field NS_109");
        check(fn.contains("PROTOCOL_SOAP11_TOKEN"), "field PROTOCOL_SOAP11_TOKEN");
        check(fn.contains("PROTOCOL_SOAP12_TOKEN"), "field PROTOCOL_SOAP12_TOKEN");
        check(fn.contains("PROTOCOL_XML_TOKEN"), "field PROTOCOL_XML_TOKEN");
        check(fn.contains("QNAME_CHAIN_PORT_PATTERN"), "field QNAME_CHAIN_PORT_PATTERN");
        check(fn.contains("QNAME_CHAIN_PROTOCOL_BINDING"), "field QNAME_CHAIN_PROTOCOL_BINDING");
        check(fn.contains("QNAME_CHAIN_SERVICE_PATTERN"), "field QNAME_CHAIN_SERVICE_PATTERN");
        check(fn.contains("QNAME_HANDLER"), "field QNAME_HANDLER");
        check(fn.contains("QNAME_HANDLER_CHAIN"), "field QNAME_HANDLER_CHAIN");
        check(fn.contains("QNAME_HANDLER_CHAINS"), "field QNAME_HANDLER_CHAINS");
        check(fn.contains("QNAME_HANDLER_CLASS"), "field QNAME_HANDLER_CLASS");
        check(fn.contains("QNAME_HANDLER_HEADER"), "field QNAME_HANDLER_HEADER");
        check(fn.contains("QNAME_HANDLER_NAME"), "field QNAME_HANDLER_NAME");
        check(fn.contains("QNAME_HANDLER_PARAM"), "field QNAME_HANDLER_PARAM");
        check(fn.contains("QNAME_HANDLER_PARAM_NAME"), "field QNAME_HANDLER_PARAM_NAME");
        check(fn.contains("QNAME_HANDLER_PARAM_VALUE"), "field QNAME_HANDLER_PARAM_VALUE");
        check(fn.contains("QNAME_HANDLER_ROLE"), "field QNAME_HANDLER_ROLE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
