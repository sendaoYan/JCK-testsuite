/*
 * @test
 * @summary com.sun.xml.internal.ws.api.pipe.ThrowableContainerPropertySet conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ThrowableContainerPropertySetTest
 */

import java.lang.reflect.Modifier;


public class ThrowableContainerPropertySetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.pipe.ThrowableContainerPropertySet", false, ThrowableContainerPropertySetTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.pipe.ThrowableContainerPropertySet"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.message.BasePropertySet"), "superclass com.oracle.webservices.internal.api.message.BasePropertySet");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getFaultMessage"), "method getFaultMessage");
        check(mn.contains("getPropertyMap"), "method getPropertyMap");
        check(mn.contains("getResponsePacket"), "method getResponsePacket");
        check(mn.contains("getThrowable"), "method getThrowable");
        check(mn.contains("isFaultCreated"), "method isFaultCreated");
        check(mn.contains("setFaultCreated"), "method setFaultCreated");
        check(mn.contains("setFaultMessage"), "method setFaultMessage");
        check(mn.contains("setResponsePacket"), "method setResponsePacket");
        check(mn.contains("setThrowable"), "method setThrowable");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("FAULT_MESSAGE"), "field FAULT_MESSAGE");
        check(fn.contains("FIBER_COMPLETION_THROWABLE"), "field FIBER_COMPLETION_THROWABLE");
        check(fn.contains("IS_FAULT_CREATED"), "field IS_FAULT_CREATED");
        check(fn.contains("RESPONSE_PACKET"), "field RESPONSE_PACKET");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
