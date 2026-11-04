/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.SenderMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SenderMessagesTest
 */

import java.lang.reflect.Modifier;


public class SenderMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.SenderMessages", false, SenderMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.SenderMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("SENDER_NESTED_ERROR"), "method SENDER_NESTED_ERROR");
        check(mn.contains("SENDER_REQUEST_ILLEGAL_VALUE_FOR_CONTENT_NEGOTIATION"), "method SENDER_REQUEST_ILLEGAL_VALUE_FOR_CONTENT_NEGOTIATION");
        check(mn.contains("SENDER_REQUEST_MESSAGE_NOT_READY"), "method SENDER_REQUEST_MESSAGE_NOT_READY");
        check(mn.contains("SENDER_RESPONSE_CANNOT_DECODE_FAULT_DETAIL"), "method SENDER_RESPONSE_CANNOT_DECODE_FAULT_DETAIL");
        check(mn.contains("localizableSENDER_NESTED_ERROR"), "method localizableSENDER_NESTED_ERROR");
        check(mn.contains("localizableSENDER_REQUEST_ILLEGAL_VALUE_FOR_CONTENT_NEGOTIATION"), "method localizableSENDER_REQUEST_ILLEGAL_VALUE_FOR_CONTENT_NEGOTIATION");
        check(mn.contains("localizableSENDER_REQUEST_MESSAGE_NOT_READY"), "method localizableSENDER_REQUEST_MESSAGE_NOT_READY");
        check(mn.contains("localizableSENDER_RESPONSE_CANNOT_DECODE_FAULT_DETAIL"), "method localizableSENDER_RESPONSE_CANNOT_DECODE_FAULT_DETAIL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
