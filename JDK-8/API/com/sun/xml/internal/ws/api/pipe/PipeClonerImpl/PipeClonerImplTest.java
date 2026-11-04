/*
 * @test
 * @summary com.sun.xml.internal.ws.api.pipe.PipeClonerImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PipeClonerImplTest
 */

import java.lang.reflect.Modifier;


public class PipeClonerImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.pipe.PipeClonerImpl", false, PipeClonerImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.pipe.PipeClonerImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.pipe.PipeCloner"), "superclass com.sun.xml.internal.ws.api.pipe.PipeCloner");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "method add");
        check(mn.contains("copy"), "method copy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
