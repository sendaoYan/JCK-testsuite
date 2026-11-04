/*
 * @test
 * @summary com.sun.xml.internal.ws.api.databinding.DatabindingFactory conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DatabindingFactoryTest
 */

import java.lang.reflect.Modifier;


public class DatabindingFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.databinding.DatabindingFactory", false, DatabindingFactoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.databinding.DatabindingFactory"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.databinding.DatabindingFactory"), "superclass com.oracle.webservices.internal.api.databinding.DatabindingFactory");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createRuntime"), "method createRuntime");
        check(mn.contains("newInstance"), "method newInstance");
        check(mn.contains("properties"), "method properties");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
