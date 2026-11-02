/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.PolicyAssertion conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PolicyAssertionTest
 */

import java.lang.reflect.Modifier;


public class PolicyAssertionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.PolicyAssertion", false, PolicyAssertionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.PolicyAssertion"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getAttributeValue"), "method getAttributeValue");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("getAttributesSet"), "method getAttributesSet");
        check(mn.contains("getImplementation"), "method getImplementation");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNestedAssertionsIterator"), "method getNestedAssertionsIterator");
        check(mn.contains("getNestedPolicy"), "method getNestedPolicy");
        check(mn.contains("getParametersIterator"), "method getParametersIterator");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("hasNestedAssertions"), "method hasNestedAssertions");
        check(mn.contains("hasNestedPolicy"), "method hasNestedPolicy");
        check(mn.contains("hasParameters"), "method hasParameters");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isIgnorable"), "method isIgnorable");
        check(mn.contains("isOptional"), "method isOptional");
        check(mn.contains("isPrivate"), "method isPrivate");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
