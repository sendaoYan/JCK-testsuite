/*
 * @test
 * @summary com.sun.xml.internal.ws.util.QNameMap.Entry conformance: reflection (kind, superclass, interfaces, methods).
 * @run main EntryTest
 */

import java.lang.reflect.Modifier;


public class EntryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.QNameMap$Entry", false, EntryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.QNameMap$Entry"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createQName"), "method createQName");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("setValue"), "method setValue");
        check(mn.contains("toString"), "method toString");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("localName"), "field localName");
        check(fn.contains("nsUri"), "field nsUri");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
