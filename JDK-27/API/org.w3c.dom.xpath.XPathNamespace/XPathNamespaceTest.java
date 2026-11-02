/*
 * @test
 * @summary org.w3c.dom.xpath.XPathNamespace conformance: namespace node.
 * @run main XPathNamespaceTest
 */

public class XPathNamespaceTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

    public static void main(String[] args) throws Exception {
        Class<?> k = L("org.w3c.dom.xpath.XPathNamespace");
        check(k.isInterface(), "org.w3c.dom.xpath.XPathNamespace is an interface");
        check(L("org.w3c.dom.Node").isAssignableFrom(k), "XPathNamespace extends Node");
        check(k.getInterfaces().length >= 1, "XPathNamespace has superinterface");
        check(sconst("org.w3c.dom.xpath.XPathNamespace", "XPATH_NAMESPACE_NODE") == 13, "XPATH_NAMESPACE_NODE == 13");
        check(k.getMethod("getOwnerElement") != null, "getOwnerElement()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
