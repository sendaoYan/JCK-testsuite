/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.AttachmentEx.MimeHeader conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MimeHeaderTest
 */

import java.lang.reflect.Modifier;


public class MimeHeaderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.AttachmentEx$MimeHeader", false, MimeHeaderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.AttachmentEx$MimeHeader"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getValue"), "method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
