/*
 * @test
 * @summary javax.xml.crypto.dsig.dom.DOMValidateContext conformance: DOM validate context.
 * @run main DOMValidateContextTest
 */


public class DOMValidateContextTest {
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
        javax.xml.crypto.dsig.dom.DOMValidateContext c = new javax.xml.crypto.dsig.dom.DOMValidateContext(key, node);
        check(c.getNode() == node, "node");
        check(c instanceof javax.xml.crypto.dsig.XMLValidateContext, "is XMLValidateContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
