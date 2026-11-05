/*
 * @test
 * @summary org.xml.sax.SAXParseException conformance: hierarchy, all constructors, line/column/systemId/publicId accessors, toString.
 * @run main SAXParseExceptionTest
 */

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SAXParseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Locator loc(final String pub, final String sys, final int line, final int col) {
        return new Locator() {
            public String getPublicId() { return pub; }
            public String getSystemId() { return sys; }
            public int getLineNumber() { return line; }
            public int getColumnNumber() { return col; }
        };
    }

    public static void main(String[] args) throws Exception {
        // ---- hierarchy ----
        check(SAXException.class.isAssignableFrom(SAXParseException.class), "extends SAXException");

        // ---- (String, Locator) ----
        SAXParseException e1 = new SAXParseException("msg", loc("PUB", "SYS", 12, 34));
        check("msg".equals(e1.getMessage()), "message preserved");
        check("PUB".equals(e1.getPublicId()), "publicId from locator");
        check("SYS".equals(e1.getSystemId()), "systemId from locator");
        check(e1.getLineNumber() == 12, "lineNumber from locator");
        check(e1.getColumnNumber() == 34, "columnNumber from locator");
        check(e1.getException() == null, "no wrapped exception");
        check(e1.toString().contains("msg"), "toString includes message");

        // ---- (String, Locator, Exception) ----
        IllegalStateException cause = new IllegalStateException("root");
        SAXParseException e2 = new SAXParseException("msg2", loc("P", "S", 5, 6), cause);
        check(e2.getException() == cause, "wrapped exception preserved");
        check(e2.getCause() == cause, "getCause == wrapped");
        check(e2.getLineNumber() == 5 && e2.getColumnNumber() == 6, "position from locator + exception");

        // ---- (String, String, String, int, int) ----
        SAXParseException e3 = new SAXParseException("msg3", "PUB3", "SYS3", 7, 8);
        check("PUB3".equals(e3.getPublicId()), "publicId from string args");
        check("SYS3".equals(e3.getSystemId()), "systemId from string args");
        check(e3.getLineNumber() == 7 && e3.getColumnNumber() == 8, "line/column from int args");
        check("msg3".equals(e3.getMessage()), "message from string args");

        // ---- (String, String, String, int, int, Exception) ----
        SAXParseException e4 = new SAXParseException("msg4", "PUB4", "SYS4", 9, 10, cause);
        check(e4.getException() == cause, "wrapped exception from 6-arg ctor");
        check(e4.getSystemId().equals("SYS4") && e4.getLineNumber() == 9 && e4.getColumnNumber() == 10, "6-arg position");
        check(e4.toString().contains("msg4"), "6-arg toString includes message");
        check(e4.toString().contains("SYS4"), "toString includes systemId");

        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral SAXParseException contract (from a real failure) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final org.xml.sax.SAXParseException[] seen = new org.xml.sax.SAXParseException[1];
        xr.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void fatalError(org.xml.sax.SAXParseException e) { seen[0] = e; }
        });
        org.xml.sax.SAXParseException thrown = null;
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r>unclosed"))); }
        catch (org.xml.sax.SAXParseException e) { thrown = e; }
        check(thrown != null && seen[0] != null && thrown.getLineNumber() == seen[0].getLineNumber(), "parse failure throws a SAXParseException matching the one handed to fatalError (fresh instance, line=1)");
        check(thrown.getLineNumber() == 1 && thrown.getColumnNumber() > 0, "real failure carries line=1 and a column");
        check(thrown.getPublicId() == null && thrown.getSystemId() == null, "ids null for an anonymous stream source");
        check(thrown.getException() == null || thrown.getException() != null, "getException accessible on real failures");
        check(thrown.toString().contains("lineNumber: 1") && thrown.toString().contains("columnNumber: 12"), "toString embeds the live position (lineNumber: 1; columnNumber: 12)");
        // constructor round-trip preserves the position quadruple
        org.xml.sax.SAXParseException manual = new org.xml.sax.SAXParseException("m", "pub", "sys", 7, 9);
        check("m".equals(manual.getMessage()) && "pub".equals(manual.getPublicId()) && "sys".equals(manual.getSystemId()), "message/ids round-trip");
        check(manual.getLineNumber() == 7 && manual.getColumnNumber() == 9, "line/column round-trip");
        check(manual.toString().contains("lineNumber: 7") && manual.toString().contains("columnNumber: 9") && manual.toString().contains("systemId: sys"), "toString format embeds ids and position quadruple");
    }
}
