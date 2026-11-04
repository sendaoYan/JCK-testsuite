/*
 * @test
 * @summary com.sun.xml.internal.ws.db.glassfish.JAXBRIContextFactory conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JAXBRIContextFactoryTest
 */

import java.lang.reflect.Modifier;


public class JAXBRIContextFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.db.glassfish.JAXBRIContextFactory", false, JAXBRIContextFactoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.db.glassfish.JAXBRIContextFactory"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.spi.db.BindingContextFactory"), "superclass com.sun.xml.internal.ws.spi.db.BindingContextFactory");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getContext"), "method getContext");
        check(mn.contains("isFor"), "method isFor");
        check(mn.contains("newContext"), "method newContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
