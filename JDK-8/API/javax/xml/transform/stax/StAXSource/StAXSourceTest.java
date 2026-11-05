/*
 * @test
 * @summary javax.xml.transform.stax.StAXSource conformance: StAX source.
 * @run main StAXSourceTest
 */


public class StAXSourceTest {
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
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader rdr = f.createXMLStreamReader(new java.io.StringReader("<a/>"));
        javax.xml.transform.stax.StAXSource s = new javax.xml.transform.stax.StAXSource(rdr);
        check(s.getXMLStreamReader() == rdr, "stream reader");
        testCtorRoundTrips();
        testIdentityBridging();
        testXsltFromStaxSource();
        testJdk8Surface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: ctor round-trips + StAX identity bridging + XSLT closure
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static void testCtorRoundTrips() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r =
            ifac.createXMLStreamReader(new java.io.StringReader(DOC));
        javax.xml.transform.stax.StAXSource s1 = new javax.xml.transform.stax.StAXSource(r);
        check(s1.getXMLStreamReader() == r, "(XMLStreamReader) ctor round-trip");
        check(s1.getXMLEventReader() == null, "(XMLStreamReader) ctor: no event reader");
        check(s1.getSystemId() == null, "getSystemId null (derived from reader location)");
        javax.xml.stream.XMLEventReader er =
            ifac.createXMLEventReader(new java.io.StringReader(DOC));
        javax.xml.transform.stax.StAXSource s2 = new javax.xml.transform.stax.StAXSource(er);
        check(s2.getXMLEventReader() == er, "(XMLEventReader) ctor round-trip");
        check(s2.getXMLStreamReader() == null, "(XMLEventReader) ctor: no stream reader");
        boolean uoe = false;
        try { s1.setSystemId("x"); }
        catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "setSystemId -> UnsupportedOperationException");
        check("http://javax.xml.transform.stax.StAXSource/feature"
                  .equals(javax.xml.transform.stax.StAXSource.FEATURE),
              "StAXSource.FEATURE constant value");
    }

    static void testIdentityBridging() throws Exception {
        // XMLStreamReader -> XMLStreamWriter through identity transform
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLOutputFactory ofac = javax.xml.stream.XMLOutputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r =
            ifac.createXMLStreamReader(new java.io.StringReader(DOC));
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = ofac.createXMLStreamWriter(sw);
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id.transform(new javax.xml.transform.stax.StAXSource(r),
                     new javax.xml.transform.stax.StAXResult(w));
        check(sw.toString().replace("\r", "").endsWith(DOC),
              "identity transform bridges StAX reader -> StAX writer (document intact)");
        check(sw.toString().startsWith("<?xml"), "StAX bridged output carries a declaration");
    }

    static void testXsltFromStaxSource() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.transform.Transformer t =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer(
                new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                    "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                  + "<xsl:template match=\"/\"><out><xsl:value-of select=\"root/item[2]/@name\"/></out>"
                  + "</xsl:template></xsl:stylesheet>")));
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        javax.xml.stream.XMLStreamReader r =
            ifac.createXMLStreamReader(new java.io.StringReader(DOC));
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stax.StAXSource(r),
                    new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals("<out>alpha</out>"),
              "XSLT transform reading from StAXSource (output exact)");
    }

    static void testJdk8Surface() throws Exception {
        boolean absent = false;
        try { javax.xml.transform.stax.StAXSource.class.getMethod("isEmpty"); }
        catch (NoSuchMethodException e) { absent = true; }
        check(absent, "JDK-8: StAXSource has no isEmpty()");
    }
}
