/*
 * @test
 * @summary com.sun.xml.internal.ws.message.AttachmentSetImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AttachmentSetImplTest
 */

import java.lang.reflect.Modifier;


public class AttachmentSetImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.message.AttachmentSetImpl", false, AttachmentSetImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.message.AttachmentSetImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.message.AttachmentSet"), "implements com.sun.xml.internal.ws.api.message.AttachmentSet");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "method add");
        check(mn.contains("get"), "method get");
        check(mn.contains("isEmpty"), "method isEmpty");
        check(mn.contains("iterator"), "method iterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
