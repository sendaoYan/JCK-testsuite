/*
 * @test
 * @summary com.sun.xml.internal.ws.util.VersionUtil conformance: reflection (kind, superclass, interfaces, methods).
 * @run main VersionUtilTest
 */

import java.lang.reflect.Modifier;


public class VersionUtilTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.VersionUtil", false, VersionUtilTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.VersionUtil"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("compare"), "method compare");
        check(mn.contains("getCanonicalVersion"), "method getCanonicalVersion");
        check(mn.contains("getValidVersionString"), "method getValidVersionString");
        check(mn.contains("isValidVersion"), "method isValidVersion");
        check(mn.contains("isVersion20"), "method isVersion20");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("JAXWS_VERSION_20"), "field JAXWS_VERSION_20");
        check(fn.contains("JAXWS_VERSION_DEFAULT"), "field JAXWS_VERSION_DEFAULT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
