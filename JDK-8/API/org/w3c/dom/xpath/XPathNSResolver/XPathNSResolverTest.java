/*
 * @test
 * @summary org.w3c.dom.xpath.XPathNSResolver conformance: reflection shape checks.
 * @run main XPathNSResolverTest
 */

import java.lang.reflect.*;

public class XPathNSResolverTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathNSResolver");
        check(c != null, "org.w3c.dom.xpath.XPathNSResolver loads");
        check(c.getName().equals("org.w3c.dom.xpath.XPathNSResolver"), "binary name is org.w3c.dom.xpath.XPathNSResolver");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.xpath.XPathNSResolver is public");
        check(c.isInterface(), "org.w3c.dom.xpath.XPathNSResolver is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "lookupNamespaceURI"), "declares lookupNamespaceURI");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: XPathNSResolver shape (JDK 8 reflection) ----
    static void deepContract() throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathNSResolver");
        check(c.isInterface(), "XPathNSResolver is an interface");
        java.lang.reflect.Method m = c.getMethod("lookupNamespaceURI", String.class);
        check(m != null, "single method lookupNamespaceURI(String)");
        check(c.getDeclaredFields().length == 0, "no constants");
    }
}
