/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.Packet.Status conformance: reflection (kind, superclass, interfaces, methods).
 * @run main StatusTest
 */

import java.lang.reflect.Modifier;


public class StatusTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.Packet$Status", false, StatusTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.Packet$Status"), "name");
        check(c.isEnum(), "enum");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isRequest"), "method isRequest");
        check(mn.contains("isResponse"), "method isResponse");
        check(mn.contains("valueOf"), "method valueOf");
        check(mn.contains("values"), "method values");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("Request"), "field Request");
        check(fn.contains("Response"), "field Response");
        check(fn.contains("Unknown"), "field Unknown");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
