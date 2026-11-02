/*
 * @test
 * @summary org.w3c.dom.xpath.XPathNSResolver conformance: namespace resolver.
 * @run main XPathNSResolverTest
 */

public class XPathNSResolverTest {
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
        Class<?> k = L("org.w3c.dom.xpath.XPathNSResolver");
        check(k.isInterface(), "org.w3c.dom.xpath.XPathNSResolver is an interface");
        check(k.getInterfaces().length == 0, "XPathNSResolver declares no superinterface");
        check(k.getMethod("lookupNamespaceURI", String.class) != null, "lookupNamespaceURI(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
