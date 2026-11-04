/*
 * @test
 * @summary javax.xml.crypto.dom.DOMStructure conformance: DOM structure.
 * @run main DOMStructureTest
 */


public class DOMStructureTest {
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
        javax.xml.crypto.dom.DOMStructure s = new javax.xml.crypto.dom.DOMStructure(node);
        check(s.getNode() == node, "node");
        check(!s.isFeatureSupported("bogus"), "feature not supported");
        check(s instanceof javax.xml.crypto.XMLStructure, "is XMLStructure");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
