/*
 * @test
 * @summary com.sun.xml.internal.ws.server.ServerPropertyConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ServerPropertyConstantsTest
 */

import java.lang.reflect.Modifier;


public class ServerPropertyConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.server.ServerPropertyConstants", false, ServerPropertyConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.server.ServerPropertyConstants"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("GET_ATTACHMENT_PROPERTY"), "field GET_ATTACHMENT_PROPERTY");
        check(fn.contains("SET_ATTACHMENT_PROPERTY"), "field SET_ATTACHMENT_PROPERTY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
