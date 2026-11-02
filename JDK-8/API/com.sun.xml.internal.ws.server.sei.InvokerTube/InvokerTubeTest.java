/*
 * @test
 * @summary com.sun.xml.internal.ws.server.sei.InvokerTube conformance: reflection (kind, superclass, interfaces, methods).
 * @run main InvokerTubeTest
 */

import java.lang.reflect.Modifier;


public class InvokerTubeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.server.sei.InvokerTube", false, InvokerTubeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.server.sei.InvokerTube"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.pipe.helper.AbstractTubeImpl"), "superclass com.sun.xml.internal.ws.api.pipe.helper.AbstractTubeImpl");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.server.sei.InvokerSource"), "implements com.sun.xml.internal.ws.server.sei.InvokerSource");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getInvoker"), "method getInvoker");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("invoker"), "field invoker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
