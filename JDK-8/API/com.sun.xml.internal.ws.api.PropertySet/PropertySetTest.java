/*
 * @test
 * @summary com.sun.xml.internal.ws.api.PropertySet conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PropertySetTest
 */

import java.lang.reflect.Modifier;


public class PropertySetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.PropertySet", false, PropertySetTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.PropertySet"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.message.BasePropertySet"), "superclass com.oracle.webservices.internal.api.message.BasePropertySet");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createEntrySet"), "method createEntrySet");
        check(mn.contains("get"), "method get");
        check(mn.contains("getPropertyMap"), "method getPropertyMap");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("put"), "method put");
        check(mn.contains("remove"), "method remove");
        check(mn.contains("supports"), "method supports");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
