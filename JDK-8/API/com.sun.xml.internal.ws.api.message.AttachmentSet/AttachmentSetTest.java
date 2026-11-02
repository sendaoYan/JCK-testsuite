/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.AttachmentSet conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AttachmentSetTest
 */

import java.lang.reflect.Modifier;


public class AttachmentSetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.AttachmentSet", false, AttachmentSetTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.AttachmentSet"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.Iterable"), "implements java.lang.Iterable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "method add");
        check(mn.contains("get"), "method get");
        check(mn.contains("isEmpty"), "method isEmpty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
