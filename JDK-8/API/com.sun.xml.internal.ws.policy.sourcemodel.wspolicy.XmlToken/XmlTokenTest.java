/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.sourcemodel.wspolicy.XmlToken conformance: reflection (kind, superclass, interfaces, methods).
 * @run main XmlTokenTest
 */

import java.lang.reflect.Modifier;


public class XmlTokenTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.sourcemodel.wspolicy.XmlToken", false, XmlTokenTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.sourcemodel.wspolicy.XmlToken"), "name");
        check(c.isEnum(), "enum");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isElement"), "method isElement");
        check(mn.contains("resolveToken"), "method resolveToken");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("valueOf"), "method valueOf");
        check(mn.contains("values"), "method values");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("All"), "field All");
        check(fn.contains("Digest"), "field Digest");
        check(fn.contains("DigestAlgorithm"), "field DigestAlgorithm");
        check(fn.contains("ExactlyOne"), "field ExactlyOne");
        check(fn.contains("Ignorable"), "field Ignorable");
        check(fn.contains("Name"), "field Name");
        check(fn.contains("Optional"), "field Optional");
        check(fn.contains("Policy"), "field Policy");
        check(fn.contains("PolicyReference"), "field PolicyReference");
        check(fn.contains("PolicyUris"), "field PolicyUris");
        check(fn.contains("UNKNOWN"), "field UNKNOWN");
        check(fn.contains("Uri"), "field Uri");
        check(fn.contains("UsingPolicy"), "field UsingPolicy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
