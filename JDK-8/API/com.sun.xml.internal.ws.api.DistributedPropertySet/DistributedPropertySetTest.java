/*
 * @test
 * @summary com.sun.xml.internal.ws.api.DistributedPropertySet conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DistributedPropertySetTest
 */

import java.lang.reflect.Modifier;


public class DistributedPropertySetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.DistributedPropertySet", false, DistributedPropertySetTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.DistributedPropertySet"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.message.BaseDistributedPropertySet"), "superclass com.oracle.webservices.internal.api.message.BaseDistributedPropertySet");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addSatellite"), "method addSatellite");
        check(mn.contains("copySatelliteInto"), "method copySatelliteInto");
        check(mn.contains("removeSatellite"), "method removeSatellite");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
