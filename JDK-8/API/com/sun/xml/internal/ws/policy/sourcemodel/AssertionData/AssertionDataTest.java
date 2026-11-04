/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.sourcemodel.AssertionData conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AssertionDataTest
 */

import java.lang.reflect.Modifier;


public class AssertionDataTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.sourcemodel.AssertionData", false, AssertionDataTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.sourcemodel.AssertionData"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.io.Serializable"), "implements java.io.Serializable");
        check(has(ifs, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clone"), "method clone");
        check(mn.contains("containsAttribute"), "method containsAttribute");
        check(mn.contains("createAssertionData"), "method createAssertionData");
        check(mn.contains("createAssertionParameterData"), "method createAssertionParameterData");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getAttributeValue"), "method getAttributeValue");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("getAttributesSet"), "method getAttributesSet");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNodeType"), "method getNodeType");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isIgnorableAttributeSet"), "method isIgnorableAttributeSet");
        check(mn.contains("isOptionalAttributeSet"), "method isOptionalAttributeSet");
        check(mn.contains("isPrivateAttributeSet"), "method isPrivateAttributeSet");
        check(mn.contains("removeAttribute"), "method removeAttribute");
        check(mn.contains("setAttribute"), "method setAttribute");
        check(mn.contains("setIgnorableAttribute"), "method setIgnorableAttribute");
        check(mn.contains("setOptionalAttribute"), "method setOptionalAttribute");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
