/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.PolicyConstants conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PolicyConstantsTest
 */

import java.lang.reflect.Modifier;


public class PolicyConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.PolicyConstants", false, PolicyConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.PolicyConstants"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("CLIENT_CONFIGURATION_IDENTIFIER"), "field CLIENT_CONFIGURATION_IDENTIFIER");
        check(fn.contains("SUN_MANAGEMENT_NAMESPACE"), "field SUN_MANAGEMENT_NAMESPACE");
        check(fn.contains("SUN_POLICY_NAMESPACE_PREFIX"), "field SUN_POLICY_NAMESPACE_PREFIX");
        check(fn.contains("SUN_POLICY_NAMESPACE_URI"), "field SUN_POLICY_NAMESPACE_URI");
        check(fn.contains("VISIBILITY_ATTRIBUTE"), "field VISIBILITY_ATTRIBUTE");
        check(fn.contains("VISIBILITY_VALUE_PRIVATE"), "field VISIBILITY_VALUE_PRIVATE");
        check(fn.contains("WSU_ID"), "field WSU_ID");
        check(fn.contains("WSU_NAMESPACE_PREFIX"), "field WSU_NAMESPACE_PREFIX");
        check(fn.contains("WSU_NAMESPACE_URI"), "field WSU_NAMESPACE_URI");
        check(fn.contains("XML_ID"), "field XML_ID");
        check(fn.contains("XML_NAMESPACE_URI"), "field XML_NAMESPACE_URI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
