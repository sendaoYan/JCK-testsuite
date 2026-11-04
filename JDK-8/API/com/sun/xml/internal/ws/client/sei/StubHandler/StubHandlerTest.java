/*
 * @test
 * @summary com.sun.xml.internal.ws.client.sei.StubHandler conformance: reflection (kind, superclass, interfaces, methods).
 * @run main StubHandlerTest
 */

import java.lang.reflect.Modifier;


public class StubHandlerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.sei.StubHandler", false, StubHandlerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.sei.StubHandler"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.databinding.ClientCallBridge"), "implements com.sun.xml.internal.ws.api.databinding.ClientCallBridge");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createRequestPacket"), "method createRequestPacket");
        check(mn.contains("getMethod"), "method getMethod");
        check(mn.contains("getOperationModel"), "method getOperationModel");
        check(mn.contains("getOperationName"), "method getOperationName");
        check(mn.contains("getSoapAction"), "method getSoapAction");
        check(mn.contains("initArgs"), "method initArgs");
        check(mn.contains("isOneWay"), "method isOneWay");
        check(mn.contains("readResponse"), "method readResponse");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("checkedExceptions"), "field checkedExceptions");
        check(fn.contains("isOneWay"), "field isOneWay");
        check(fn.contains("javaMethod"), "field javaMethod");
        check(fn.contains("packetFactory"), "field packetFactory");
        check(fn.contains("responseBuilder"), "field responseBuilder");
        check(fn.contains("soapAction"), "field soapAction");
        check(fn.contains("soapVersion"), "field soapVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
