/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.MessageHeaders conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MessageHeadersTest
 */

import java.lang.reflect.Modifier;


public class MessageHeadersTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.MessageHeaders", false, MessageHeadersTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.MessageHeaders"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "method add");
        check(mn.contains("addOrReplace"), "method addOrReplace");
        check(mn.contains("asList"), "method asList");
        check(mn.contains("get"), "method get");
        check(mn.contains("getHeaders"), "method getHeaders");
        check(mn.contains("getNotUnderstoodHeaders"), "method getNotUnderstoodHeaders");
        check(mn.contains("getUnderstoodHeaders"), "method getUnderstoodHeaders");
        check(mn.contains("hasHeaders"), "method hasHeaders");
        check(mn.contains("isUnderstood"), "method isUnderstood");
        check(mn.contains("remove"), "method remove");
        check(mn.contains("replace"), "method replace");
        check(mn.contains("understood"), "method understood");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
