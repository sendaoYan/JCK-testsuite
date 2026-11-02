/*
 * @test
 * @summary com.sun.xml.internal.ws.streaming.Attributes conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AttributesTest
 */

import java.lang.reflect.Modifier;


public class AttributesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.streaming.Attributes", false, AttributesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.streaming.Attributes"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getIndex"), "method getIndex");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("getLocalName"), "method getLocalName");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("isNamespaceDeclaration"), "method isNamespaceDeclaration");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
