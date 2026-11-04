/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.Policy conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PolicyTest
 */

import java.lang.reflect.Modifier;


public class PolicyTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.Policy", false, PolicyTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.Policy"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.Iterable"), "implements java.lang.Iterable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("contains"), "method contains");
        check(mn.contains("createEmptyPolicy"), "method createEmptyPolicy");
        check(mn.contains("createNullPolicy"), "method createNullPolicy");
        check(mn.contains("createPolicy"), "method createPolicy");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getId"), "method getId");
        check(mn.contains("getIdOrName"), "method getIdOrName");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespaceVersion"), "method getNamespaceVersion");
        check(mn.contains("getNumberOfAssertionSets"), "method getNumberOfAssertionSets");
        check(mn.contains("getVocabulary"), "method getVocabulary");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isEmpty"), "method isEmpty");
        check(mn.contains("isNull"), "method isNull");
        check(mn.contains("iterator"), "method iterator");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
