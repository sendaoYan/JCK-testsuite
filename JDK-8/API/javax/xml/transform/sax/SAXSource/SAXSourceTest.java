/*
 * @test
 * @summary javax.xml.transform.sax.SAXSource conformance: SAX source.
 * @run main SAXSourceTest
 */


public class SAXSourceTest {
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
        javax.xml.transform.sax.SAXSource s = new javax.xml.transform.sax.SAXSource();
        check(s.getXMLReader() == null, "reader null");
        check(s.getInputSource() == null, "input source null");
        s.setSystemId("sysid");
        check("sysid".equals(s.getSystemId()), "systemId");
        testCtorMatrix();
        testSourceToInputSource();
        testIdentityWithExplicitReader();
        testJdk8Surface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: constructor matrix + sourceToInputSource + transform through explicit XMLReader
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item></root>";

    static void testCtorMatrix() {
        org.xml.sax.InputSource is = new org.xml.sax.InputSource(new java.io.StringReader(DOC));
        javax.xml.transform.sax.SAXSource s1 = new javax.xml.transform.sax.SAXSource(is);
        check(s1.getInputSource() == is, "(InputSource) ctor round-trip");
        check(s1.getXMLReader() == null, "(InputSource) ctor: no reader yet");
        javax.xml.transform.sax.SAXSource s2 = new javax.xml.transform.sax.SAXSource();
        check(s2.getInputSource() == null && s2.getXMLReader() == null, "no-arg ctor empty");
        s2.setSystemId("http://base/x.xml");
        check("http://base/x.xml".equals(s2.getSystemId()), "setSystemId round-trip");
        check("http://javax.xml.transform.sax.SAXSource/feature"
                  .equals(javax.xml.transform.sax.SAXSource.FEATURE),
              "SAXSource.FEATURE constant value");
    }

    static void testSourceToInputSource() {
        javax.xml.transform.stream.StreamSource withReader =
            new javax.xml.transform.stream.StreamSource(
                new java.io.StringReader(DOC), "http://base/x.xml");
        org.xml.sax.InputSource conv1 =
            javax.xml.transform.sax.SAXSource.sourceToInputSource(withReader);
        check("http://base/x.xml".equals(conv1.getSystemId()),
              "sourceToInputSource(StreamSource w/ reader): systemId kept");
        check(conv1.getCharacterStream() != null,
              "sourceToInputSource(StreamSource w/ reader): character stream carried");
        javax.xml.transform.stream.StreamSource withStream =
            new javax.xml.transform.stream.StreamSource(
                new java.io.ByteArrayInputStream(DOC.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        org.xml.sax.InputSource conv2 =
            javax.xml.transform.sax.SAXSource.sourceToInputSource(withStream);
        check(conv2.getByteStream() != null,
              "sourceToInputSource(StreamSource w/ stream): byte stream carried");
    }

    static void testIdentityWithExplicitReader() throws Exception {
        org.xml.sax.XMLReader xr =
            javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        javax.xml.transform.sax.SAXSource src =
            new javax.xml.transform.sax.SAXSource(xr,
                new org.xml.sax.InputSource(new java.io.StringReader(DOC)));
        check(src.getXMLReader() == xr, "(XMLReader,InputSource) ctor keeps the reader");
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        id.transform(src, new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals(DOC),
              "identity transform drives the caller-supplied XMLReader (output exact)");
    }

    static void testJdk8Surface() throws Exception {
        boolean absent = false;
        try { javax.xml.transform.sax.SAXSource.class.getMethod("isEmpty"); }
        catch (NoSuchMethodException e) { absent = true; }
        check(absent, "JDK-8: SAXSource has no isEmpty()");
    }
}
