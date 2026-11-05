/*
 * @test
 * @summary javax.xml.transform.stream.StreamSource conformance: stream source.
 * @run main StreamSourceTest
 */


public class StreamSourceTest {
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
        javax.xml.transform.stream.StreamSource s = new javax.xml.transform.stream.StreamSource();
        check(s.getInputStream() == null, "input stream null");
        check(s.getReader() == null, "reader null");
        s.setSystemId("sysid");
        check("sysid".equals(s.getSystemId()), "systemId");
        s.setPublicId("pub");
        check("pub".equals(s.getPublicId()), "publicId");
        testCtorFamily();
        testTransformFromFile();
        testJdk8Surface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: full constructor family + File-based transform closure
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item></root>";

    static void testCtorFamily() {
        java.io.ByteArrayInputStream in =
            new java.io.ByteArrayInputStream(DOC.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        javax.xml.transform.stream.StreamSource s1 =
            new javax.xml.transform.stream.StreamSource(in);
        check(s1.getInputStream() == in, "(InputStream) ctor round-trip");
        javax.xml.transform.stream.StreamSource s2 =
            new javax.xml.transform.stream.StreamSource(in, "http://base/a.xml");
        check(s2.getInputStream() == in && "http://base/a.xml".equals(s2.getSystemId()),
              "(InputStream,systemId) ctor");
        java.io.StringReader rd = new java.io.StringReader(DOC);
        javax.xml.transform.stream.StreamSource s3 =
            new javax.xml.transform.stream.StreamSource(rd, "http://base/b.xml");
        check(s3.getReader() == rd && "http://base/b.xml".equals(s3.getSystemId()),
              "(Reader,systemId) ctor");
        javax.xml.transform.stream.StreamSource s4 =
            new javax.xml.transform.stream.StreamSource("http://base/c.xml");
        check("http://base/c.xml".equals(s4.getSystemId()), "(systemId) ctor");
        check(s4.getInputStream() == null && s4.getReader() == null && s4.getPublicId() == null,
              "(systemId) ctor leaves streams null");
        javax.xml.transform.stream.StreamSource s5 =
            new javax.xml.transform.stream.StreamSource();
        java.io.StringReader rd2 = new java.io.StringReader(DOC);
        s5.setReader(rd2);
        check(s5.getReader() == rd2, "setReader/getReader round-trip");
        java.io.ByteArrayInputStream in2 =
            new java.io.ByteArrayInputStream(new byte[]{'<','a','/','>'});
        s5.setInputStream(in2);
        check(s5.getInputStream() == in2, "setInputStream/getInputStream round-trip");
        check("http://javax.xml.transform.stream.StreamSource/feature"
                  .equals(javax.xml.transform.stream.StreamSource.FEATURE),
              "StreamSource.FEATURE constant value");
    }

    static void testTransformFromFile() throws Exception {
        java.io.File tmp = java.io.File.createTempFile("stsrc", ".xml");
        try {
            java.io.FileOutputStream fo = new java.io.FileOutputStream(tmp);
            fo.write(DOC.getBytes("UTF-8"));
            fo.close();
            // (File) ctor derives a systemId from the absolute path
            javax.xml.transform.stream.StreamSource fs =
                new javax.xml.transform.stream.StreamSource(tmp);
            check(fs.getSystemId() != null && fs.getSystemId().endsWith(".xml"),
                  "(File) ctor derives systemId from the file path");
            javax.xml.transform.Transformer id =
                javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            id.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            java.io.StringWriter sw = new java.io.StringWriter();
            id.transform(fs, new javax.xml.transform.stream.StreamResult(sw));
            check(sw.toString().replace("\r", "").equals(DOC),
                  "identity transform reads a File-based StreamSource (output exact)");
        } finally {
            tmp.delete();
        }
    }

    static void testJdk8Surface() throws Exception {
        boolean absent = false;
        try { javax.xml.transform.stream.StreamSource.class.getMethod("isEmpty"); }
        catch (NoSuchMethodException e) { absent = true; }
        check(absent, "JDK-8: StreamSource has no isEmpty()");
    }
}
