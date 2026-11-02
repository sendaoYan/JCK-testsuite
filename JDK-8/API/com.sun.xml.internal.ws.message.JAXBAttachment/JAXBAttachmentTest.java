/*
 * @test
 * @summary com.sun.xml.internal.ws.message.JAXBAttachment conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JAXBAttachmentTest
 */

import java.lang.reflect.Modifier;


public class JAXBAttachmentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.message.JAXBAttachment", false, JAXBAttachmentTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.message.JAXBAttachment"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.message.Attachment"), "implements com.sun.xml.internal.ws.api.message.Attachment");
        check(has(ifs, "javax.activation.DataSource"), "implements javax.activation.DataSource");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("asByteArray"), "method asByteArray");
        check(mn.contains("asDataHandler"), "method asDataHandler");
        check(mn.contains("asInputStream"), "method asInputStream");
        check(mn.contains("asSource"), "method asSource");
        check(mn.contains("getContentId"), "method getContentId");
        check(mn.contains("getContentType"), "method getContentType");
        check(mn.contains("getInputStream"), "method getInputStream");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getOutputStream"), "method getOutputStream");
        check(mn.contains("writeTo"), "method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
