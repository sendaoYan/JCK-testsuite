/*
 * @test
 * @summary com.sun.xml.internal.ws.spi.db.FieldSetter conformance: reflection (kind, superclass, interfaces, methods).
 * @run main FieldSetterTest
 */

import java.lang.reflect.Modifier;


public class FieldSetterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.spi.db.FieldSetter", false, FieldSetterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.spi.db.FieldSetter"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.spi.db.PropertySetterBase"), "superclass com.sun.xml.internal.ws.spi.db.PropertySetterBase");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getField"), "method getField");
        check(mn.contains("set"), "method set");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("field"), "field field");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
