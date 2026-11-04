/*
 * @test
 * @summary com.sun.xml.internal.ws.api.server.Adapter conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AdapterTest
 */

import java.lang.reflect.Modifier;


public class AdapterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.server.Adapter", false, AdapterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.server.Adapter"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.Component"), "implements com.sun.xml.internal.ws.api.Component");
        check(has(ifs, "com.sun.xml.internal.ws.api.config.management.Reconfigurable"), "implements com.sun.xml.internal.ws.api.config.management.Reconfigurable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createToolkit"), "method createToolkit");
        check(mn.contains("getEndpoint"), "method getEndpoint");
        check(mn.contains("getEndpointComponent"), "method getEndpointComponent");
        check(mn.contains("getPool"), "method getPool");
        check(mn.contains("getSPI"), "method getSPI");
        check(mn.contains("reconfigure"), "method reconfigure");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("endpoint"), "field endpoint");
        check(fn.contains("pool"), "field pool");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
