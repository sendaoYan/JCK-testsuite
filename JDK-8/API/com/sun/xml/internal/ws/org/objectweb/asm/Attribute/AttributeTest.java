/*
 * @test
 * @summary com.sun.xml.internal.ws.org.objectweb.asm.Attribute conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AttributeTest
 */

import java.lang.reflect.Modifier;


public class AttributeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.org.objectweb.asm.Attribute", false, AttributeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.org.objectweb.asm.Attribute"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLabels"), "method getLabels");
        check(mn.contains("isCodeAttribute"), "method isCodeAttribute");
        check(mn.contains("isUnknown"), "method isUnknown");
        check(mn.contains("read"), "method read");
        check(mn.contains("write"), "method write");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("type"), "field type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
