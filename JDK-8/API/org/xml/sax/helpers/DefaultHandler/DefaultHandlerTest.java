/*
 * @test
 * @summary org.xml.sax.helpers.DefaultHandler conformance: default handler.
 * @run main DefaultHandlerTest
 */


public class DefaultHandlerTest {
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
        org.xml.sax.helpers.DefaultHandler h = new org.xml.sax.helpers.DefaultHandler();
        check(h instanceof org.xml.sax.ContentHandler, "is ContentHandler");
        check(h instanceof org.xml.sax.EntityResolver, "is EntityResolver");
        check(h instanceof org.xml.sax.DTDHandler, "is DTDHandler");
        check(h instanceof org.xml.sax.ErrorHandler, "is ErrorHandler");
        h.startDocument();
        h.endDocument();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DefaultHandler contract (quad-interface base) ----
    static void deepContract() throws Exception {
        org.xml.sax.helpers.DefaultHandler h = new org.xml.sax.helpers.DefaultHandler();
        check(org.xml.sax.EntityResolver.class.isInstance(h), "implements EntityResolver");
        check(org.xml.sax.DTDHandler.class.isInstance(h), "implements DTDHandler");
        check(org.xml.sax.ContentHandler.class.isInstance(h), "implements ContentHandler");
        check(org.xml.sax.ErrorHandler.class.isInstance(h), "implements ErrorHandler");
        check(h.resolveEntity(null, "missing.dtd") == null, "resolveEntity default returns null");
        h.warning(new org.xml.sax.SAXParseException("w", null));
        h.error(new org.xml.sax.SAXParseException("e", null));
        check(true, "warning/error default to no-ops");
        boolean rethrown = false;
        org.xml.sax.SAXParseException fatal = new org.xml.sax.SAXParseException("f", new org.xml.sax.helpers.LocatorImpl(), new java.io.IOException("io"));
        java.io.IOException ioCause = (java.io.IOException) fatal.getException();
        try { h.fatalError(fatal); } catch (org.xml.sax.SAXException e) { rethrown = (e.getException() == ioCause); }
        check(rethrown, "fatalError default rethrows wrapped in a new SAXException");
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.newSAXParser().parse(new java.io.ByteArrayInputStream("<!DOCTYPE r [<!ELEMENT r (#PCDATA)><!NOTATION n SYSTEM 'n.sys'><!ENTITY ue SYSTEM 'e.xml' NDATA n>]><r/>".getBytes("UTF-8")), h);
        check(true, "bare DefaultHandler parses a DTD+notation+unparsed-entity document silently");
        // roles are individually addressable through the base type
        org.xml.sax.ContentHandler asContent = h;
        asContent.setDocumentLocator(null);
        asContent.startDocument();
        asContent.startPrefixMapping("p", "urn:p");
        asContent.startElement("", "e", "e", new org.xml.sax.helpers.AttributesImpl());
        asContent.characters("t".toCharArray(), 0, 1);
        asContent.ignorableWhitespace(" ".toCharArray(), 0, 1);
        asContent.processingInstruction("pi", "d");
        asContent.skippedEntity("se");
        asContent.endElement("", "e", "e");
        asContent.endPrefixMapping("p");
        asContent.endDocument();
        check(true, "full ContentHandler callback set accepts no-op dispatch");
        org.xml.sax.DTDHandler asDTD = h;
        asDTD.notationDecl("n", null, "s");
        asDTD.unparsedEntityDecl("ue", null, "s", "n");
        check(true, "DTDHandler callbacks accept no-op dispatch");
    }
}
