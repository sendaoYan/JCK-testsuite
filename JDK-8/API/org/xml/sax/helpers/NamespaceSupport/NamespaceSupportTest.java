/*
 * @test
 * @summary org.xml.sax.helpers.NamespaceSupport conformance: namespace support.
 * @run main NamespaceSupportTest
 */


public class NamespaceSupportTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        org.xml.sax.helpers.NamespaceSupport ns = new org.xml.sax.helpers.NamespaceSupport();
        check(ns.declarePrefix("p", "urn:test"), "declare prefix");
        check("urn:test".equals(ns.getURI("p")), "get uri");
        check("p".equals(ns.getPrefix("urn:test")), "get prefix");
        ns.pushContext();
        check(ns.declarePrefix("q", "urn:q"), "push declare");
        ns.popContext();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
