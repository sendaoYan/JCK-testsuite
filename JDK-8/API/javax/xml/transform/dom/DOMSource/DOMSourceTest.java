/*
 * @test
 * @summary javax.xml.transform.dom.DOMSource conformance: DOM source.
 * @run main DOMSourceTest
 */


public class DOMSourceTest {
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
        javax.xml.transform.dom.DOMSource s = new javax.xml.transform.dom.DOMSource();
        check(s.getNode() == null, "node null");
        s.setSystemId("sysid");
        check("sysid".equals(s.getSystemId()), "systemId");
        testCtorMatrix();
        testIdentityClosure();
        testJdk8Surface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: constructor matrix + identity transform closure
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item></root>";

    static void testCtorMatrix() throws Exception {
        javax.xml.parsers.DocumentBuilder db =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(new java.io.ByteArrayInputStream(DOC.getBytes("UTF-8")));
        javax.xml.transform.dom.DOMSource s1 = new javax.xml.transform.dom.DOMSource(doc);
        check(s1.getNode() == doc, "(Document) ctor node round-trip");
        check(s1.getSystemId() == null, "(Document) ctor systemId null");
        org.w3c.dom.Element el = doc.getDocumentElement();
        javax.xml.transform.dom.DOMSource s2 =
            new javax.xml.transform.dom.DOMSource(el, "http://base/el");
        check(s2.getNode() == el, "(Node,systemId) ctor node round-trip");
        check("http://base/el".equals(s2.getSystemId()), "(Node,systemId) ctor systemId round-trip");
        javax.xml.transform.dom.DOMSource s3 = new javax.xml.transform.dom.DOMSource();
        check(s3.getNode() == null && s3.getSystemId() == null, "no-arg ctor empty");
        s3.setNode(el);
        check(s3.getNode() == el, "setNode/getNode round-trip");
        s3.setSystemId("sid2");
        check("sid2".equals(s3.getSystemId()), "setSystemId overrides");
        check("http://javax.xml.transform.dom.DOMSource/feature"
                  .equals(javax.xml.transform.dom.DOMSource.FEATURE),
              "DOMSource.FEATURE constant value");
    }

    static void testIdentityClosure() throws Exception {
        javax.xml.parsers.DocumentBuilder db =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(new java.io.ByteArrayInputStream(DOC.getBytes("UTF-8")));
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        id.transform(new javax.xml.transform.dom.DOMSource(doc),
                     new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals(DOC),
              "identity DOMSource->StreamResult reproduces the document exactly");
        // element subtree as source
        javax.xml.transform.dom.DOMSource sub =
            new javax.xml.transform.dom.DOMSource(doc.getDocumentElement());
        java.io.StringWriter sw2 = new java.io.StringWriter();
        id.transform(sub, new javax.xml.transform.stream.StreamResult(sw2));
        check(sw2.toString().replace("\r", "").contains("first"),
              "DOMSource over element subtree transforms the subtree");
    }

    static void testJdk8Surface() throws Exception {
        boolean absent = false;
        try { javax.xml.transform.dom.DOMSource.class.getMethod("isEmpty"); }
        catch (NoSuchMethodException e) { absent = true; }
        check(absent, "JDK-8: DOMSource has no isEmpty()");
    }
}
