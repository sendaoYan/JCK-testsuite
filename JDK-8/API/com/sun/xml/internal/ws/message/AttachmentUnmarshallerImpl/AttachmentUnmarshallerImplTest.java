/*
 * @test
 * @summary com.sun.xml.internal.ws.message.AttachmentUnmarshallerImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AttachmentUnmarshallerImplTest
 */

import java.lang.reflect.Modifier;


public class AttachmentUnmarshallerImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.message.AttachmentUnmarshallerImpl", false, AttachmentUnmarshallerImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.message.AttachmentUnmarshallerImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("javax.xml.bind.attachment.AttachmentUnmarshaller"), "superclass javax.xml.bind.attachment.AttachmentUnmarshaller");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAttachmentAsByteArray"), "method getAttachmentAsByteArray");
        check(mn.contains("getAttachmentAsDataHandler"), "method getAttachmentAsDataHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
