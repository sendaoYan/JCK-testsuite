/*
 * @test
 * @summary javax.xml.crypto.dom.DOMCryptoContext conformance: DOM crypto context.
 * @run main DOMCryptoContextTest
 */


public class DOMCryptoContextTest {
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
        javax.xml.crypto.dom.DOMCryptoContext c = new javax.xml.crypto.dom.DOMCryptoContext() {};
        check(c.getKeySelector() == null, "key selector null");
        c.setBaseURI("base");
        check("base".equals(c.getBaseURI()), "base uri");
        c.setDefaultNamespacePrefix("d");
        check("d".equals(c.getDefaultNamespacePrefix()), "default prefix");
        c.setProperty("k", "v");
        check("v".equals(c.getProperty("k")), "property");
        check(c.iterator() != null, "iterator non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
