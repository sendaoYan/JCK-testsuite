/*
 * @test
 * @summary javax.xml.transform.stream.StreamResult conformance: stream result.
 * @run main StreamResultTest
 */


public class StreamResultTest {
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
        javax.xml.transform.stream.StreamResult r = new javax.xml.transform.stream.StreamResult();
        check(r.getOutputStream() == null, "output stream null");
        check(r.getWriter() == null, "writer null");
        r.setSystemId("sysid");
        check("sysid".equals(r.getSystemId()), "systemId");
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.stream.StreamResult r2 = new javax.xml.transform.stream.StreamResult(sw);
        check(r2.getWriter() == sw, "writer set");
        testCtorFamily();
        testTransformClosures();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: full constructor family + real transform closures
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item></root>";

    static void testCtorFamily() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.transform.stream.StreamResult r1 =
            new javax.xml.transform.stream.StreamResult(bos);
        check(r1.getOutputStream() == bos, "(OutputStream) ctor round-trip");
        check(r1.getSystemId() == null, "(OutputStream) ctor systemId null");
        java.io.StringWriter w = new java.io.StringWriter();
        javax.xml.transform.stream.StreamResult r3 =
            new javax.xml.transform.stream.StreamResult(w);
        check(r3.getWriter() == w, "(Writer) ctor round-trip");
        r3.setSystemId("http://base/w");
        check(r3.getWriter() == w && "http://base/w".equals(r3.getSystemId()),
              "systemId independent of the writer sink");
        javax.xml.transform.stream.StreamResult r4 =
            new javax.xml.transform.stream.StreamResult("sysid-only");
        check("sysid-only".equals(r4.getSystemId()), "(systemId) ctor");
        check(r4.getOutputStream() == null && r4.getWriter() == null,
              "(systemId) ctor leaves sinks null");
        java.io.File tmp = java.io.File.createTempFile("stres", ".xml");
        try {
            javax.xml.transform.stream.StreamResult r5 =
                new javax.xml.transform.stream.StreamResult(tmp);
            check(r5.getSystemId() != null && r5.getSystemId().endsWith(".xml"),
                  "(File) ctor derives systemId from the file path");
            javax.xml.transform.stream.StreamResult r6 =
                new javax.xml.transform.stream.StreamResult();
            r6.setSystemId(tmp);
            check(r6.getSystemId() != null && r6.getSystemId().endsWith(".xml"),
                  "setSystemId(File) derives file URL");
        } finally {
            tmp.delete();
        }
        javax.xml.transform.stream.StreamResult r7 =
            new javax.xml.transform.stream.StreamResult();
        java.io.StringWriter w2 = new java.io.StringWriter();
        r7.setWriter(w2);
        check(r7.getWriter() == w2, "setWriter/getWriter round-trip");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        r7.setOutputStream(bos2);
        check(r7.getOutputStream() == bos2, "setOutputStream/getOutputStream round-trip");
        check("http://javax.xml.transform.stream.StreamResult/feature"
                  .equals(javax.xml.transform.stream.StreamResult.FEATURE),
              "StreamResult.FEATURE constant value");
    }

    static void testTransformClosures() throws Exception {
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        // to OutputStream: UTF-8 bytes decode to the exact document
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        id.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        id.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                     new javax.xml.transform.stream.StreamResult(bos));
        check(new String(bos.toByteArray(), "UTF-8").replace("\r", "").equals(DOC),
              "identity transform to StreamResult(OutputStream): exact UTF-8 bytes");
        // to Writer
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.Transformer id2 =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id2.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        id2.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                      new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals(DOC),
              "identity transform to StreamResult(Writer): exact characters");
    }
}
