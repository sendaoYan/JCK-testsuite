/*
 * @test
 * @summary javax.xml.transform.dom.DOMResult conformance: DOM result.
 * @run main DOMResultTest
 */


public class DOMResultTest {
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
        javax.xml.transform.dom.DOMResult r = new javax.xml.transform.dom.DOMResult();
        check(r.getNode() == null, "node null");
        check(r.getNextSibling() == null, "next sibling null");
        r.setSystemId("sysid");
        check("sysid".equals(r.getSystemId()), "systemId");
        testCtorMatrix();
        testTransformIntoDomResult();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: constructor matrix + real Stream->DOMResult closure
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static void testCtorMatrix() throws Exception {
        javax.xml.parsers.DocumentBuilder db =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = db.newDocument();
        javax.xml.transform.dom.DOMResult r1 = new javax.xml.transform.dom.DOMResult(d);
        check(r1.getNode() == d && r1.getNextSibling() == null, "(Node) ctor");
        check(r1.getSystemId() == null, "(Node) ctor systemId null");
        javax.xml.transform.dom.DOMResult r2 =
            new javax.xml.transform.dom.DOMResult(d, "http://base/out");
        check(r2.getNode() == d && "http://base/out".equals(r2.getSystemId()),
              "(Node,systemId) ctor");
        org.w3c.dom.Element e = d.createElement("anchor");
        d.appendChild(e);
        org.w3c.dom.Element sib = d.createElement("sib");
        e.appendChild(sib); // nextSibling must be contained by node
        javax.xml.transform.dom.DOMResult r3 = new javax.xml.transform.dom.DOMResult(e, sib, "sid");
        // negative: nextSibling not contained by node -> IllegalArgumentException
        org.w3c.dom.Element orphan = d.createElement("orphan");
        boolean iae = false;
        try {
            new javax.xml.transform.dom.DOMResult(e, orphan, "sid");
        } catch (IllegalArgumentException ex) {
            iae = ex.getMessage() != null && ex.getMessage().contains("nextSibling");
        }
        check(iae, "(Node,nextSibling,systemId) with uncontained sibling -> IAE");
        check(r3.getNode() == e, "(Node,nextSibling,systemId) node");
        check(r3.getNextSibling() == sib, "(Node,nextSibling,systemId) nextSibling");
        check("sid".equals(r3.getSystemId()), "(Node,nextSibling,systemId) systemId");
        // negative: setNode(null) rejected while a nextSibling is set -> IllegalStateException
        boolean ise = false;
        try {
            r3.setNode(null);
        } catch (IllegalStateException ex) {
            ise = ex.getMessage() != null && ex.getMessage().contains("nextSibling");
        }
        check(ise, "setNode(null) with live nextSibling -> ISE");
        r3.setNextSibling(null);
        check(r3.getNextSibling() == null, "setNextSibling(null) stored");
        r3.setNode(null);
        check(r3.getNode() == null, "setNode(null) stored once nextSibling cleared");
        check("http://javax.xml.transform.dom.DOMResult/feature"
                  .equals(javax.xml.transform.dom.DOMResult.FEATURE),
              "DOMResult.FEATURE constant value");
    }

    static void testTransformIntoDomResult() throws Exception {
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        javax.xml.transform.dom.DOMResult r = new javax.xml.transform.dom.DOMResult();
        id.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)), r);
        org.w3c.dom.Node n = r.getNode();
        check(n != null && n.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE,
              "transform into fresh DOMResult yields a Document");
        org.w3c.dom.Element root = ((org.w3c.dom.Document) n).getDocumentElement();
        check("root".equals(root.getNodeName()), "result root name");
        check(root.getChildNodes().getLength() == 3, "result root has 3 children");
        check("1".equals(root.getChildNodes().item(1).getAttributes().getNamedItem("id").getNodeValue()),
              "result second item @id");
        check("first".equals(root.getChildNodes().item(1).getTextContent()),
              "result second item text");
    }
}
