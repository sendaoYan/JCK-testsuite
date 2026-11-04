/*
 * @test
 * @summary com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ManagementAssertionTest
 */

import java.lang.reflect.Modifier;


public class ManagementAssertionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion", false, ManagementAssertionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.policy.SimpleAssertion"), "superclass com.sun.xml.internal.ws.policy.SimpleAssertion");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAssertion"), "method getAssertion");
        check(mn.contains("getId"), "method getId");
        check(mn.contains("getStart"), "method getStart");
        check(mn.contains("isManagementEnabled"), "method isManagementEnabled");
        check(mn.contains("monitoringAttribute"), "method monitoringAttribute");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("MANAGEMENT_ATTRIBUTE_QNAME"), "field MANAGEMENT_ATTRIBUTE_QNAME");
        check(fn.contains("MONITORING_ATTRIBUTE_QNAME"), "field MONITORING_ATTRIBUTE_QNAME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
