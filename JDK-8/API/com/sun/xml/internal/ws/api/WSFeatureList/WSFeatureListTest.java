/*
 * @test
 * @summary com.sun.xml.internal.ws.api.WSFeatureList conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSFeatureListTest
 */

import java.lang.reflect.Modifier;


public class WSFeatureListTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.WSFeatureList", false, WSFeatureListTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.WSFeatureList"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.Iterable"), "implements java.lang.Iterable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("get"), "method get");
        check(mn.contains("isEnabled"), "method isEnabled");
        check(mn.contains("mergeFeatures"), "method mergeFeatures");
        check(mn.contains("toArray"), "method toArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
