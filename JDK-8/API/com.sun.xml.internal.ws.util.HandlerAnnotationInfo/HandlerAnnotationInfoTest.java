/*
 * @test
 * @summary com.sun.xml.internal.ws.util.HandlerAnnotationInfo conformance: reflection (kind, superclass, interfaces, methods).
 * @run main HandlerAnnotationInfoTest
 */

import java.lang.reflect.Modifier;


public class HandlerAnnotationInfoTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.HandlerAnnotationInfo", false, HandlerAnnotationInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.HandlerAnnotationInfo"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getHandlers"), "method getHandlers");
        check(mn.contains("getRoles"), "method getRoles");
        check(mn.contains("setHandlers"), "method setHandlers");
        check(mn.contains("setRoles"), "method setRoles");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
