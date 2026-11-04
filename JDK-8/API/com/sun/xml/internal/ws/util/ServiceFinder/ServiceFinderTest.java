/*
 * @test
 * @summary com.sun.xml.internal.ws.util.ServiceFinder conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ServiceFinderTest
 */

import java.lang.reflect.Modifier;


public class ServiceFinderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.ServiceFinder", false, ServiceFinderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.ServiceFinder"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.Iterable"), "implements java.lang.Iterable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("find"), "method find");
        check(mn.contains("iterator"), "method iterator");
        check(mn.contains("toArray"), "method toArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
