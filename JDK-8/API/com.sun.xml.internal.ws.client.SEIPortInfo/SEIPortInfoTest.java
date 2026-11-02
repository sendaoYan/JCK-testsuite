/*
 * @test
 * @summary com.sun.xml.internal.ws.client.SEIPortInfo conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SEIPortInfoTest
 */

import java.lang.reflect.Modifier;


public class SEIPortInfoTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.SEIPortInfo", false, SEIPortInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.SEIPortInfo"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.client.PortInfo"), "superclass com.sun.xml.internal.ws.client.PortInfo");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createBinding"), "method createBinding");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("model"), "field model");
        check(fn.contains("sei"), "field sei");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
