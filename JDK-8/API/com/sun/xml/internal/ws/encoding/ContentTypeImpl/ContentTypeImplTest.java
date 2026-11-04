/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.ContentTypeImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ContentTypeImplTest
 */

import java.lang.reflect.Modifier;


public class ContentTypeImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.ContentTypeImpl", false, ContentTypeImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.ContentTypeImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.pipe.ContentType"), "implements com.sun.xml.internal.ws.api.pipe.ContentType");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAcceptHeader"), "method getAcceptHeader");
        check(mn.contains("getBoundary"), "method getBoundary");
        check(mn.contains("getBoundaryParameter"), "method getBoundaryParameter");
        check(mn.contains("getCharSet"), "method getCharSet");
        check(mn.contains("getContentType"), "method getContentType");
        check(mn.contains("getRootId"), "method getRootId");
        check(mn.contains("getSOAPActionHeader"), "method getSOAPActionHeader");
        check(mn.contains("setAcceptHeader"), "method setAcceptHeader");
        check(mn.contains("setBoundary"), "method setBoundary");
        check(mn.contains("setBoundaryParameter"), "method setBoundaryParameter");
        check(mn.contains("setRootId"), "method setRootId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
