/*
 * @test
 * @summary com.sun.xml.internal.ws.api.config.management.policy.ManagedServiceAssertion conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ManagedServiceAssertionTest
 */

import java.lang.reflect.Modifier;


public class ManagedServiceAssertionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.config.management.policy.ManagedServiceAssertion", false, ManagedServiceAssertionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.config.management.policy.ManagedServiceAssertion"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion"), "superclass com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAssertion"), "method getAssertion");
        check(mn.contains("getCommunicationServerImplementations"), "method getCommunicationServerImplementations");
        check(mn.contains("getConfigReaderImplementation"), "method getConfigReaderImplementation");
        check(mn.contains("getConfigSaverImplementation"), "method getConfigSaverImplementation");
        check(mn.contains("getConfiguratorImplementation"), "method getConfiguratorImplementation");
        check(mn.contains("getEndpointDisposeDelay"), "method getEndpointDisposeDelay");
        check(mn.contains("isManagementEnabled"), "method isManagementEnabled");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("MANAGED_SERVICE_QNAME"), "field MANAGED_SERVICE_QNAME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
