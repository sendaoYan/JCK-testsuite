/*
 * @test
 * @summary com.sun.xml.internal.ws.spi.db.TypeInfo conformance: reflection (kind, superclass, interfaces, methods).
 * @run main TypeInfoTest
 */

import java.lang.reflect.Modifier;


public class TypeInfoTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.spi.db.TypeInfo", false, TypeInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.spi.db.TypeInfo"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("get"), "method get");
        check(mn.contains("getGenericType"), "method getGenericType");
        check(mn.contains("getItemType"), "method getItemType");
        check(mn.contains("getParentCollectionType"), "method getParentCollectionType");
        check(mn.contains("isGlobalElement"), "method isGlobalElement");
        check(mn.contains("isNillable"), "method isNillable");
        check(mn.contains("isRepeatedElement"), "method isRepeatedElement");
        check(mn.contains("properties"), "method properties");
        check(mn.contains("setGenericType"), "method setGenericType");
        check(mn.contains("setGlobalElement"), "method setGlobalElement");
        check(mn.contains("setNillable"), "method setNillable");
        check(mn.contains("setParentCollectionType"), "method setParentCollectionType");
        check(mn.contains("toItemType"), "method toItemType");
        check(mn.contains("toString"), "method toString");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("annotations"), "field annotations");
        check(fn.contains("tagName"), "field tagName");
        check(fn.contains("type"), "field type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
