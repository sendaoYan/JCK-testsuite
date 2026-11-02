/*
 * @test
 * @summary com.sun.xml.internal.ws.util.DOMUtil conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DOMUtilTest
 */

import java.lang.reflect.Modifier;


public class DOMUtilTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.DOMUtil", false, DOMUtilTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.DOMUtil"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createDom"), "method createDom");
        check(mn.contains("getChildElements"), "method getChildElements");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getFirstElementChild"), "method getFirstElementChild");
        check(mn.contains("serializeNode"), "method serializeNode");
        check(mn.contains("writeTagWithAttributes"), "method writeTagWithAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
