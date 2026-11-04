/*
 * @test
 * @summary com.sun.xml.internal.ws.assembler.dev.TubelineAssemblyDecorator conformance: reflection (kind, superclass, interfaces, methods).
 * @run main TubelineAssemblyDecoratorTest
 */

import java.lang.reflect.Modifier;


public class TubelineAssemblyDecoratorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.assembler.dev.TubelineAssemblyDecorator", false, TubelineAssemblyDecoratorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.assembler.dev.TubelineAssemblyDecorator"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("composite"), "method composite");
        check(mn.contains("decorateClient"), "method decorateClient");
        check(mn.contains("decorateClientHead"), "method decorateClientHead");
        check(mn.contains("decorateClientTail"), "method decorateClientTail");
        check(mn.contains("decorateServer"), "method decorateServer");
        check(mn.contains("decorateServerHead"), "method decorateServerHead");
        check(mn.contains("decorateServerTail"), "method decorateServerTail");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
