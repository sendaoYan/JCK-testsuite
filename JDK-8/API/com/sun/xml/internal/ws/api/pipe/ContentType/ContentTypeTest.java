/*
 * @test
 * @summary com.sun.xml.internal.ws.api.pipe.ContentType conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ContentTypeTest
 */

import java.lang.reflect.Modifier;


public class ContentTypeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.pipe.ContentType", false, ContentTypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.pipe.ContentType"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.oracle.webservices.internal.api.message.ContentType"), "implements com.oracle.webservices.internal.api.message.ContentType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
