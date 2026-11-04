/*
 * @test
 * @summary com.sun.xml.internal.ws.addressing.WsaPropertyBag conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WsaPropertyBagTest
 */

import java.lang.reflect.Modifier;


public class WsaPropertyBagTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.addressing.WsaPropertyBag", false, WsaPropertyBagTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.addressing.WsaPropertyBag"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.message.BasePropertySet"), "superclass com.oracle.webservices.internal.api.message.BasePropertySet");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAction"), "method getAction");
        check(mn.contains("getFaultToFromRequest"), "method getFaultToFromRequest");
        check(mn.contains("getFrom"), "method getFrom");
        check(mn.contains("getMessageID"), "method getMessageID");
        check(mn.contains("getMessageIdFromRequest"), "method getMessageIdFromRequest");
        check(mn.contains("getPropertyMap"), "method getPropertyMap");
        check(mn.contains("getReplyToFromRequest"), "method getReplyToFromRequest");
        check(mn.contains("getTo"), "method getTo");
        check(mn.contains("getToAsReference"), "method getToAsReference");
        check(mn.contains("setFaultToFromRequest"), "method setFaultToFromRequest");
        check(mn.contains("setMessageIdFromRequest"), "method setMessageIdFromRequest");
        check(mn.contains("setReplyToFromRequest"), "method setReplyToFromRequest");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("WSA_FAULTTO_FROM_REQUEST"), "field WSA_FAULTTO_FROM_REQUEST");
        check(fn.contains("WSA_MSGID_FROM_REQUEST"), "field WSA_MSGID_FROM_REQUEST");
        check(fn.contains("WSA_REPLYTO_FROM_REQUEST"), "field WSA_REPLYTO_FROM_REQUEST");
        check(fn.contains("WSA_TO"), "field WSA_TO");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
