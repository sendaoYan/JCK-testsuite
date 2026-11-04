/*
 * @test
 * @summary com.sun.xml.internal.ws.server.provider.ProviderInvokerTube conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ProviderInvokerTubeTest
 */

import java.lang.reflect.Modifier;


public class ProviderInvokerTubeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.server.provider.ProviderInvokerTube", false, ProviderInvokerTubeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.server.provider.ProviderInvokerTube"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.server.InvokerTube"), "superclass com.sun.xml.internal.ws.server.InvokerTube");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("create"), "method create");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("argsBuilder"), "field argsBuilder");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
