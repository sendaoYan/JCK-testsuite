/*
 * @test
 * @summary com.sun.xml.internal.ws.api.server.SDDocument conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SDDocumentTest
 */

import java.lang.reflect.Modifier;


public class SDDocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.server.SDDocument", false, SDDocumentTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.server.SDDocument"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getImports"), "method getImports");
        check(mn.contains("getRootName"), "method getRootName");
        check(mn.contains("getURL"), "method getURL");
        check(mn.contains("isSchema"), "method isSchema");
        check(mn.contains("isWSDL"), "method isWSDL");
        check(mn.contains("writeTo"), "method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
