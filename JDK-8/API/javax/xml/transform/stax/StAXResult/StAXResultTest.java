/*
 * @test
 * @summary javax.xml.transform.stax.StAXResult conformance: StAX result.
 * @run main StAXResultTest
 */


public class StAXResultTest {
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
        javax.xml.stream.XMLOutputFactory f = javax.xml.stream.XMLOutputFactory.newFactory();
        javax.xml.stream.XMLStreamWriter w = f.createXMLStreamWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r = new javax.xml.transform.stax.StAXResult(w);
        check(r.getXMLStreamWriter() == w, "stream writer");
        testCtorRoundTrips();
        testTransformIntoStaxResult();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: ctor round-trips + real transform into StAXResult closures
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static void testCtorRoundTrips() throws Exception {
        javax.xml.stream.XMLOutputFactory ofac = javax.xml.stream.XMLOutputFactory.newFactory();
        javax.xml.stream.XMLStreamWriter w =
            ofac.createXMLStreamWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r1 = new javax.xml.transform.stax.StAXResult(w);
        check(r1.getXMLStreamWriter() == w, "(XMLStreamWriter) ctor round-trip");
        check(r1.getXMLEventWriter() == null, "(XMLStreamWriter) ctor: no event writer");
        check(r1.getSystemId() == null, "getSystemId null");
        javax.xml.stream.XMLEventWriter ew =
            ofac.createXMLEventWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r2 = new javax.xml.transform.stax.StAXResult(ew);
        check(r2.getXMLEventWriter() == ew, "(XMLEventWriter) ctor round-trip");
        check(r2.getXMLStreamWriter() == null, "(XMLEventWriter) ctor: no stream writer");
        boolean uoe = false;
        try { r1.setSystemId("x"); }
        catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "setSystemId -> UnsupportedOperationException");
        check("http://javax.xml.transform.stax.StAXResult/feature"
                  .equals(javax.xml.transform.stax.StAXResult.FEATURE),
              "StAXResult.FEATURE constant value");
    }

    static void testTransformIntoStaxResult() throws Exception {
        javax.xml.stream.XMLOutputFactory ofac = javax.xml.stream.XMLOutputFactory.newFactory();
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        // Stream -> StAXResult(XMLStreamWriter): writer receives the full document
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = ofac.createXMLStreamWriter(sw);
        id.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                     new javax.xml.transform.stax.StAXResult(w));
        check(sw.toString().replace("\r", "").endsWith(DOC),
              "identity Stream->StAXResult(XMLStreamWriter) writes exact document");
        // Stream -> StAXResult(XMLEventWriter)
        java.io.StringWriter sw2 = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter ew = ofac.createXMLEventWriter(sw2);
        id.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                     new javax.xml.transform.stax.StAXResult(ew));
        String out = sw2.toString().replace("\r", "");
        check(out.contains("<root>") && out.contains("third") && out.endsWith("</root>"),
              "identity Stream->StAXResult(XMLEventWriter) writes document");
    }
}
