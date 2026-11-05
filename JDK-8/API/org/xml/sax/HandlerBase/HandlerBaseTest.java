/*
 * @test
 * @summary org.xml.sax.HandlerBase conformance: SAX handler base.
 * @run main HandlerBaseTest
 */


public class HandlerBaseTest {
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
        org.xml.sax.HandlerBase h = new org.xml.sax.HandlerBase();
        check(h instanceof org.xml.sax.DocumentHandler, "is DocumentHandler");
        check(h instanceof org.xml.sax.ErrorHandler, "is ErrorHandler");
        h.startDocument();
        h.endDocument();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral HandlerBase contract (SAX1 default base) ----
    static void deepContract() throws Exception {
        org.xml.sax.HandlerBase hb = new org.xml.sax.HandlerBase();
        check(org.xml.sax.DocumentHandler.class.isInstance(hb), "HandlerBase implements DocumentHandler");
        check(org.xml.sax.EntityResolver.class.isInstance(hb), "HandlerBase implements EntityResolver");
        check(org.xml.sax.DTDHandler.class.isInstance(hb), "HandlerBase implements DTDHandler");
        check(org.xml.sax.ErrorHandler.class.isInstance(hb), "HandlerBase implements ErrorHandler");
        check(hb.resolveEntity("pub", "sys") == null, "default resolveEntity returns null (default resolution)");
        // all overridables are no-ops: a full parse through the bare base completes silently
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.Parser p1 = spf.newSAXParser().getParser();
        p1.setDocumentHandler(hb);
        p1.setErrorHandler(hb);
        p1.setEntityResolver(hb);
        p1.setDTDHandler(hb);
        p1.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!NOTATION n SYSTEM 'x'><!ENTITY u SYSTEM 'y' NDATA n>]><r a='1'>T<?pi x?></r>")));
        check(true, "bare HandlerBase swallows a full document (all four roles no-op)");
        // subclass recording still receives events
        final java.util.List<String> got = new java.util.ArrayList<String>();
        org.xml.sax.HandlerBase rec = new org.xml.sax.HandlerBase() {
            public void startElement(String name, org.xml.sax.AttributeList a) { got.add(name + "#" + a.getLength()); }
            public void characters(char[] c, int s, int n) { got.add(new String(c, s, n)); }
        };
        org.xml.sax.Parser p2 = spf.newSAXParser().getParser();
        p2.setDocumentHandler(rec);
        p2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r x='1'>T</r>")));
        check(got.contains("r#1") && got.contains("T"), "subclass overrides receive the SAX1 events");
    }
}
