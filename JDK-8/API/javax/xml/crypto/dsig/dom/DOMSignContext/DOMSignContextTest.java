/*
 * @test
 * @summary javax.xml.crypto.dsig.dom.DOMSignContext conformance: DOM sign context.
 * @run main DOMSignContextTest
 */


public class DOMSignContextTest {
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
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        org.w3c.dom.Node node = doc.createElement("root");
        java.security.Key key = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        javax.xml.crypto.dsig.dom.DOMSignContext c = new javax.xml.crypto.dsig.dom.DOMSignContext(key, node);
        check(c.getParent() == node, "parent");
        check(c.getNextSibling() == null, "next sibling null");
        check(c instanceof javax.xml.crypto.dsig.XMLSignContext, "is XMLSignContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
