/*
 * @test
 * @summary com.sun.xml.internal.ws.spi.db.BindingInfo conformance: reflection (kind, superclass, interfaces, methods).
 * @run main BindingInfoTest
 */

import java.lang.reflect.Modifier;


public class BindingInfoTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.spi.db.BindingInfo", false, BindingInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.spi.db.BindingInfo"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("contentClasses"), "method contentClasses");
        check(mn.contains("getClassLoader"), "method getClassLoader");
        check(mn.contains("getDatabindingMode"), "method getDatabindingMode");
        check(mn.contains("getDefaultNamespace"), "method getDefaultNamespace");
        check(mn.contains("getSEIModel"), "method getSEIModel");
        check(mn.contains("getWsdlURL"), "method getWsdlURL");
        check(mn.contains("properties"), "method properties");
        check(mn.contains("setClassLoader"), "method setClassLoader");
        check(mn.contains("setDatabindingMode"), "method setDatabindingMode");
        check(mn.contains("setDefaultNamespace"), "method setDefaultNamespace");
        check(mn.contains("setSEIModel"), "method setSEIModel");
        check(mn.contains("setWsdlURL"), "method setWsdlURL");
        check(mn.contains("subclassReplacements"), "method subclassReplacements");
        check(mn.contains("typeInfos"), "method typeInfos");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("classLoader"), "field classLoader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
