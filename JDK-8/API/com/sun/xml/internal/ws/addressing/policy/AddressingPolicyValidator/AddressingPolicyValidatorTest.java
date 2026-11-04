/*
 * @test
 * @summary com.sun.xml.internal.ws.addressing.policy.AddressingPolicyValidator conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AddressingPolicyValidatorTest
 */

import java.lang.reflect.Modifier;


public class AddressingPolicyValidatorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.addressing.policy.AddressingPolicyValidator", false, AddressingPolicyValidatorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.addressing.policy.AddressingPolicyValidator"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.policy.spi.PolicyAssertionValidator"), "implements com.sun.xml.internal.ws.policy.spi.PolicyAssertionValidator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("declareSupportedDomains"), "method declareSupportedDomains");
        check(mn.contains("validateClientSide"), "method validateClientSide");
        check(mn.contains("validateServerSide"), "method validateServerSide");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
