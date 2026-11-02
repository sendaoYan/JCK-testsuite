/*
 * @test
 * @summary com.sun.xml.internal.ws.util.QNameMap conformance: reflection (kind, superclass, interfaces, methods).
 * @run main QNameMapTest
 */

import java.lang.reflect.Modifier;


public class QNameMapTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.QNameMap", false, QNameMapTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.QNameMap"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("containsKey"), "method containsKey");
        check(mn.contains("entrySet"), "method entrySet");
        check(mn.contains("get"), "method get");
        check(mn.contains("getOne"), "method getOne");
        check(mn.contains("isEmpty"), "method isEmpty");
        check(mn.contains("keySet"), "method keySet");
        check(mn.contains("put"), "method put");
        check(mn.contains("putAll"), "method putAll");
        check(mn.contains("size"), "method size");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("values"), "method values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
