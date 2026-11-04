/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.ParameterBinding conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ParameterBindingTest
 */

import java.lang.reflect.Modifier;


public class ParameterBindingTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.ParameterBinding", false, ParameterBindingTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.ParameterBinding"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createAttachment"), "method createAttachment");
        check(mn.contains("getMimeType"), "method getMimeType");
        check(mn.contains("isAttachment"), "method isAttachment");
        check(mn.contains("isBody"), "method isBody");
        check(mn.contains("isHeader"), "method isHeader");
        check(mn.contains("isUnbound"), "method isUnbound");
        check(mn.contains("toString"), "method toString");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("BODY"), "field BODY");
        check(fn.contains("HEADER"), "field HEADER");
        check(fn.contains("UNBOUND"), "field UNBOUND");
        check(fn.contains("kind"), "field kind");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
