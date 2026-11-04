/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.PolicyMapMutator conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PolicyMapMutatorTest
 */

import java.lang.reflect.Modifier;


public class PolicyMapMutatorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.PolicyMapMutator", false, PolicyMapMutatorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.PolicyMapMutator"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("connect"), "method connect");
        check(mn.contains("disconnect"), "method disconnect");
        check(mn.contains("getMap"), "method getMap");
        check(mn.contains("isConnected"), "method isConnected");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
