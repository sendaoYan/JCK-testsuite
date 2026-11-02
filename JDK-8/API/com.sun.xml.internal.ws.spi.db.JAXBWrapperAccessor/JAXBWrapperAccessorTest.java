/*
 * @test
 * @summary com.sun.xml.internal.ws.spi.db.JAXBWrapperAccessor conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JAXBWrapperAccessorTest
 */

import java.lang.reflect.Modifier;


public class JAXBWrapperAccessorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.spi.db.JAXBWrapperAccessor", false, JAXBWrapperAccessorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.spi.db.JAXBWrapperAccessor"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.spi.db.WrapperAccessor"), "superclass com.sun.xml.internal.ws.spi.db.WrapperAccessor");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createPropertyGetter"), "method createPropertyGetter");
        check(mn.contains("createPropertySetter"), "method createPropertySetter");
        check(mn.contains("getAllFields"), "method getAllFields");
        check(mn.contains("getDeclaredFields"), "method getDeclaredFields");
        check(mn.contains("getPropertyAccessor"), "method getPropertyAccessor");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("contentClass"), "field contentClass");
        check(fn.contains("elementDeclaredTypes"), "field elementDeclaredTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
