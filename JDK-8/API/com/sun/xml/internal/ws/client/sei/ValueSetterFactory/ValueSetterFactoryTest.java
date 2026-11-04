/*
 * @test
 * @summary com.sun.xml.internal.ws.client.sei.ValueSetterFactory conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ValueSetterFactoryTest
 */

import java.lang.reflect.Modifier;


public class ValueSetterFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.sei.ValueSetterFactory", false, ValueSetterFactoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.sei.ValueSetterFactory"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("get"), "method get");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("NONE"), "field NONE");
        check(fn.contains("SINGLE"), "field SINGLE");
        check(fn.contains("SYNC"), "field SYNC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
