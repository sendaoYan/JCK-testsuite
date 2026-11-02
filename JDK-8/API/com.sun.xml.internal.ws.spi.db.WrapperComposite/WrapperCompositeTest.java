/*
 * @test
 * @summary com.sun.xml.internal.ws.spi.db.WrapperComposite conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WrapperCompositeTest
 */

import java.lang.reflect.Modifier;


public class WrapperCompositeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.spi.db.WrapperComposite", false, WrapperCompositeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.spi.db.WrapperComposite"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("bridges"), "field bridges");
        check(fn.contains("values"), "field values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
