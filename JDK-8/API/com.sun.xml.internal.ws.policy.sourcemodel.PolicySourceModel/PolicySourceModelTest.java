/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.sourcemodel.PolicySourceModel conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PolicySourceModelTest
 */

import java.lang.reflect.Modifier;


public class PolicySourceModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.sourcemodel.PolicySourceModel", false, PolicySourceModelTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.sourcemodel.PolicySourceModel"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clone"), "method clone");
        check(mn.contains("containsPolicyReferences"), "method containsPolicyReferences");
        check(mn.contains("createPolicySourceModel"), "method createPolicySourceModel");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("expand"), "method expand");
        check(mn.contains("getNamespaceVersion"), "method getNamespaceVersion");
        check(mn.contains("getPolicyId"), "method getPolicyId");
        check(mn.contains("getPolicyName"), "method getPolicyName");
        check(mn.contains("getRootNode"), "method getRootNode");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
