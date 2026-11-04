/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.AddressingUtils conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AddressingUtilsTest
 */

import java.lang.reflect.Modifier;


public class AddressingUtilsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.AddressingUtils", false, AddressingUtilsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.AddressingUtils"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("fillRequestAddressingHeaders"), "method fillRequestAddressingHeaders");
        check(mn.contains("getAction"), "method getAction");
        check(mn.contains("getFaultTo"), "method getFaultTo");
        check(mn.contains("getFirstHeader"), "method getFirstHeader");
        check(mn.contains("getMessageID"), "method getMessageID");
        check(mn.contains("getRelatesTo"), "method getRelatesTo");
        check(mn.contains("getReplyTo"), "method getReplyTo");
        check(mn.contains("getTo"), "method getTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
