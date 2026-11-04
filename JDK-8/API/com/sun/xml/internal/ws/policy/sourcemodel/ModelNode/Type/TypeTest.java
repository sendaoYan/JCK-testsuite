/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.sourcemodel.ModelNode.Type conformance: reflection (kind, superclass, interfaces, methods).
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;


public class TypeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.sourcemodel.ModelNode$Type", false, TypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.sourcemodel.ModelNode$Type"), "name");
        check(c.isEnum(), "enum");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getXmlToken"), "method getXmlToken");
        check(mn.contains("valueOf"), "method valueOf");
        check(mn.contains("values"), "method values");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ALL"), "field ALL");
        check(fn.contains("ASSERTION"), "field ASSERTION");
        check(fn.contains("ASSERTION_PARAMETER_NODE"), "field ASSERTION_PARAMETER_NODE");
        check(fn.contains("EXACTLY_ONE"), "field EXACTLY_ONE");
        check(fn.contains("POLICY"), "field POLICY");
        check(fn.contains("POLICY_REFERENCE"), "field POLICY_REFERENCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
