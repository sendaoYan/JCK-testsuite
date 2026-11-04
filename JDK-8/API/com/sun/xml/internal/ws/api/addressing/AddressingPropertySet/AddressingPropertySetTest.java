/*
 * @test
 * @summary com.sun.xml.internal.ws.api.addressing.AddressingPropertySet conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AddressingPropertySetTest
 */

import java.lang.reflect.Modifier;


public class AddressingPropertySetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.addressing.AddressingPropertySet", false, AddressingPropertySetTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.addressing.AddressingPropertySet"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.message.BasePropertySet"), "superclass com.oracle.webservices.internal.api.message.BasePropertySet");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getFaultTo"), "method getFaultTo");
        check(mn.contains("getMessageId"), "method getMessageId");
        check(mn.contains("getPropertyMap"), "method getPropertyMap");
        check(mn.contains("getRelatesTo"), "method getRelatesTo");
        check(mn.contains("getReplyTo"), "method getReplyTo");
        check(mn.contains("setFaultTo"), "method setFaultTo");
        check(mn.contains("setMessageId"), "method setMessageId");
        check(mn.contains("setRelatesTo"), "method setRelatesTo");
        check(mn.contains("setReplyTo"), "method setReplyTo");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ADDRESSING_FAULT_TO"), "field ADDRESSING_FAULT_TO");
        check(fn.contains("ADDRESSING_MESSAGE_ID"), "field ADDRESSING_MESSAGE_ID");
        check(fn.contains("ADDRESSING_RELATES_TO"), "field ADDRESSING_RELATES_TO");
        check(fn.contains("ADDRESSING_REPLY_TO"), "field ADDRESSING_REPLY_TO");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
