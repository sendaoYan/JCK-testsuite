/*
 * @test
 * @summary com.sun.xml.internal.ws.util.NamespaceSupport conformance: reflection (kind, superclass, interfaces, methods).
 * @run main NamespaceSupportTest
 */

import java.lang.reflect.Modifier;


public class NamespaceSupportTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.NamespaceSupport", false, NamespaceSupportTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.NamespaceSupport"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("declarePrefix"), "method declarePrefix");
        check(mn.contains("getDeclaredPrefixes"), "method getDeclaredPrefixes");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getPrefixes"), "method getPrefixes");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("popContext"), "method popContext");
        check(mn.contains("processName"), "method processName");
        check(mn.contains("pushContext"), "method pushContext");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("slideContextDown"), "method slideContextDown");
        check(mn.contains("slideContextUp"), "method slideContextUp");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("XMLNS"), "field XMLNS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
