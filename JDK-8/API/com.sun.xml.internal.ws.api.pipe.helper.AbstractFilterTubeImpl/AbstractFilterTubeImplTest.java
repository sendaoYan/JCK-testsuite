/*
 * @test
 * @summary com.sun.xml.internal.ws.api.pipe.helper.AbstractFilterTubeImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AbstractFilterTubeImplTest
 */

import java.lang.reflect.Modifier;


public class AbstractFilterTubeImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.pipe.helper.AbstractFilterTubeImpl", false, AbstractFilterTubeImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.pipe.helper.AbstractFilterTubeImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.pipe.helper.AbstractTubeImpl"), "superclass com.sun.xml.internal.ws.api.pipe.helper.AbstractTubeImpl");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("preDestroy"), "method preDestroy");
        check(mn.contains("processException"), "method processException");
        check(mn.contains("processRequest"), "method processRequest");
        check(mn.contains("processResponse"), "method processResponse");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("next"), "field next");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
