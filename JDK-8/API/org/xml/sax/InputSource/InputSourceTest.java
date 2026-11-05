/*
 * @test
 * @summary org.xml.sax.InputSource conformance: SAX input source.
 * @run main InputSourceTest
 */


public class InputSourceTest {
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
        org.xml.sax.InputSource s = new org.xml.sax.InputSource();
        s.setPublicId("pub");
        check("pub".equals(s.getPublicId()), "publicId");
        s.setSystemId("sys");
        check("sys".equals(s.getSystemId()), "systemId");
        s.setEncoding("UTF-8");
        check("UTF-8".equals(s.getEncoding()), "encoding");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral InputSource contract (constructors + property channels) ----
    static void deepContract() throws Exception {
        org.xml.sax.InputSource e = new org.xml.sax.InputSource();
        check(e.getPublicId() == null && e.getSystemId() == null, "empty constructor: ids null");
        check(e.getByteStream() == null && e.getCharacterStream() == null && e.getEncoding() == null, "empty constructor: streams/encoding null");
        org.xml.sax.InputSource s = new org.xml.sax.InputSource("sys.xml");
        check("sys.xml".equals(s.getSystemId()), "(String) constructor sets systemId");
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream("<a/>".getBytes("UTF-8"));
        org.xml.sax.InputSource b = new org.xml.sax.InputSource(bis);
        check(b.getByteStream() == bis, "(InputStream) constructor sets byteStream");
        java.io.StringReader sr = new java.io.StringReader("<a/>");
        org.xml.sax.InputSource r = new org.xml.sax.InputSource(sr);
        check(r.getCharacterStream() == sr, "(Reader) constructor sets characterStream");
        // setters/getters round-trip, byte and char streams independent
        e.setPublicId("pub"); e.setSystemId("sys2"); e.setEncoding("UTF-16");
        check("pub".equals(e.getPublicId()) && "sys2".equals(e.getSystemId()) && "UTF-16".equals(e.getEncoding()), "id/encoding round-trip");
        e.setByteStream(bis);
        check(e.getByteStream() == bis && e.getCharacterStream() == null, "byteStream set leaves characterStream null");
        e.setCharacterStream(sr);
        check(e.getCharacterStream() == sr && e.getByteStream() == bis, "both streams coexist");
        // real parse through an InputSource carrying a Reader
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final String[] root = new String[]{null};
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String l, String q, org.xml.sax.Attributes a) { if (root[0] == null) root[0] = q; }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<fromReader/>")));
        check("fromReader".equals(root[0]), "parser consumes the InputSource characterStream");
    }
}
