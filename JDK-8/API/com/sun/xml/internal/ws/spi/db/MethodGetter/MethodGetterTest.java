/*
 * @test
 * @summary com.sun.xml.internal.ws.spi.db.MethodGetter conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MethodGetterTest
 */

import java.lang.reflect.Modifier;


public class MethodGetterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.spi.db.MethodGetter", false, MethodGetterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.spi.db.MethodGetter"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.spi.db.PropertyGetterBase"), "superclass com.sun.xml.internal.ws.spi.db.PropertyGetterBase");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("get"), "method get");
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getMethod"), "method getMethod");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
