/*
 * @test
 * @summary org.xml.sax.helpers.ParserAdapter conformance: parser adapter (no SAX1 driver available).
 * @run main ParserAdapterTest
 */


public class ParserAdapterTest {
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
        check(org.xml.sax.XMLReader.class.isAssignableFrom(org.xml.sax.helpers.ParserAdapter.class), "is XMLReader");
        check(org.xml.sax.DocumentHandler.class.isAssignableFrom(org.xml.sax.helpers.ParserAdapter.class), "is DocumentHandler");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.xml.sax.helpers.ParserAdapter.class.getMethods()) names.add(m.getName());
        check(names.contains("parse"), "has parse");
        check(names.contains("setContentHandler"), "has setContentHandler");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral ParserAdapter contract (SAX1 -> SAX2 bridging) ----
    static void deepContract() throws Exception {
        boolean npe = false;
        try { new org.xml.sax.helpers.ParserAdapter((org.xml.sax.Parser) null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "constructor with null Parser -> NullPointerException");
        org.xml.sax.Parser sax1 = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getParser();
        org.xml.sax.helpers.ParserAdapter pa = new org.xml.sax.helpers.ParserAdapter(sax1);
        check(org.xml.sax.XMLReader.class.isInstance(pa), "ParserAdapter is an XMLReader");
        check(pa.getFeature("http://xml.org/sax/features/namespaces"), "namespaces feature defaults to true");
        check(!pa.getFeature("http://xml.org/sax/features/namespace-prefixes"), "namespace-prefixes feature defaults to false");
        final java.util.List<String> log = new java.util.ArrayList<String>();
        final org.xml.sax.Locator[] seen = new org.xml.sax.Locator[1];
        pa.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) { seen[0] = l; log.add("loc"); }
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { log.add("se(" + u + "|" + ln + "|" + q + "|len=" + a.getLength() + ")"); }
            public void characters(char[] c, int s, int n) { log.add("ch(" + new String(c, s, n) + ")"); }
        });
        check(pa.getContentHandler() != null, "setContentHandler/getContentHandler round-trip");
        pa.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r xmlns='urn:d' a='1'>t</r>")));
        check(seen[0] != null, "setDocumentLocator forwarded to the SAX2 handler");
        check(log.contains("se(urn:d|r|r|len=1)"), "adapter synthesizes namespace-qualified startElement from SAX1 data");
        check(log.contains("ch(t)"), "character data forwarded");
        // prefix preservation via namespace-prefixes=true
        org.xml.sax.helpers.ParserAdapter pa2 = new org.xml.sax.helpers.ParserAdapter(sax1);
        pa2.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        final String[] qSeen = new String[]{null};
        pa2.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { qSeen[0] = q; }
        });
        pa2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<p:r xmlns:p='urn:p'/>")));
        check("p:r".equals(qSeen[0]), "namespace-prefixes=true keeps the raw qName");
        // non-namespace mode: raw names, empty uri/local
        pa2.setFeature("http://xml.org/sax/features/namespaces", false);
        final String[] rawSeen = new String[]{null};
        pa2.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { rawSeen[0] = u + "|" + ln + "|" + q; }
        });
        pa2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<p:r xmlns:p='urn:p'/>")));
        check("||p:r".equals(rawSeen[0]), "namespaces=false yields empty uri/local with the raw qName (observed: " + rawSeen[0] + ")");
        // DTD/error/entity roles pass through
        org.xml.sax.helpers.ParserAdapter pa3 = new org.xml.sax.helpers.ParserAdapter(sax1);
        org.xml.sax.DTDHandler dh = new org.xml.sax.helpers.DefaultHandler();
        org.xml.sax.ErrorHandler eh = new org.xml.sax.helpers.DefaultHandler();
        pa3.setDTDHandler(dh);
        pa3.setErrorHandler(eh);
        check(pa3.getDTDHandler() == dh && pa3.getErrorHandler() == eh, "DTD/error handler setters round-trip");
        pa3.setEntityResolver(null);
        check(pa3.getEntityResolver() == null, "entity resolver nullable");
    }
}
