/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.sourcemodel.wspolicy.NamespaceVersion conformance: reflection (kind, superclass, interfaces, methods).
 * @run main NamespaceVersionTest
 */

import java.lang.reflect.Modifier;


public class NamespaceVersionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.sourcemodel.wspolicy.NamespaceVersion", false, NamespaceVersionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.sourcemodel.wspolicy.NamespaceVersion"), "name");
        check(c.isEnum(), "enum");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("asQName"), "method asQName");
        check(mn.contains("getDefaultNamespacePrefix"), "method getDefaultNamespacePrefix");
        check(mn.contains("getLatestVersion"), "method getLatestVersion");
        check(mn.contains("resolveAsToken"), "method resolveAsToken");
        check(mn.contains("resolveVersion"), "method resolveVersion");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("valueOf"), "method valueOf");
        check(mn.contains("values"), "method values");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("v1_2"), "field v1_2");
        check(fn.contains("v1_5"), "field v1_5");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
