/*
 * @test
 * @summary org.w3c.dom.xpath.XPathNamespace conformance: reflection shape checks.
 * @run main XPathNamespaceTest
 */

import java.lang.reflect.*;

public class XPathNamespaceTest {
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
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathNamespace");
        check(c != null, "org.w3c.dom.xpath.XPathNamespace loads");
        check(c.getName().equals("org.w3c.dom.xpath.XPathNamespace"), "binary name is org.w3c.dom.xpath.XPathNamespace");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.xpath.XPathNamespace is public");
        check(c.isInterface(), "org.w3c.dom.xpath.XPathNamespace is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getOwnerElement"), "declares getOwnerElement");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: XPathNamespace contract (JDK 8 reflection) ----
    static void deepContract() throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathNamespace");
        check(org.w3c.dom.Node.class.isAssignableFrom(c), "XPathNamespace extends Node");
        check(c.getField("XPATH_NAMESPACE_NODE").getShort(null) == 13, "XPATH_NAMESPACE_NODE = 13");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getOwnerElement"), "getOwnerElement declared");
        check(c.getDeclaredFields().length == 1, "exactly one constant declared");
        // namespace nodes never appear in a regular DOM tree
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        d.appendChild(d.createElementNS("urn:x", "p:r"));
        for (org.w3c.dom.Node n = d.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling())
            check(n.getNodeType() != 13, "no XPathNamespace nodes in a plain DOM tree");
    }
}
