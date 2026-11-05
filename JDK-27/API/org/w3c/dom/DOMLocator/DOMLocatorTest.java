/*
 * @test
 * @summary org.w3c.dom.DOMLocator conformance: interface and methods.
 * @run main DOMLocatorTest
 */

import org.w3c.dom.DOMLocator;

public class DOMLocatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMLocator.class.isInterface(), "DOMLocator is an interface");
        check(DOMLocator.class.getMethod("getLineNumber").getReturnType() == int.class,
              "getLineNumber() returns int");
        check(DOMLocator.class.getMethod("getColumnNumber").getReturnType() == int.class,
              "getColumnNumber() returns int");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMLocator a = new org.w3c.dom.DOMLocator(){
            public int getLineNumber() { return 3; }
            public int getColumnNumber() { return 3; }
            public int getByteOffset() { return 3; }
            public int getUtf16Offset() { return 3; }
            public org.w3c.dom.Node getRelatedNode() { return null; }
            public java.lang.String getUri() { return "s"; }
        };
        check(a != null, "anon impl of DOMLocator");
        check(a.getLineNumber() == 3, "getLineNumber() returns fixed default");
        check(a.getColumnNumber() == 3, "getColumnNumber() returns fixed default");
        check(a.getByteOffset() == 3, "getByteOffset() returns fixed default");
        check(a.getUtf16Offset() == 3, "getUtf16Offset() returns fixed default");
        check(a.getRelatedNode() == null, "getRelatedNode() returns fixed default");
        check("s".equals(a.getUri()), "getUri() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMLocator contract (from a real parse error) ----
    static void deepContract() throws Exception {
        final org.w3c.dom.DOMLocator[] box = new org.w3c.dom.DOMLocator[]{null};
        org.w3c.dom.DOMErrorHandler h = new org.w3c.dom.DOMErrorHandler() {
            public boolean handleError(org.w3c.dom.DOMError error) { if (box[0] == null) box[0] = error.getLocation(); return true; }
        };
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) d.getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p.getDomConfig().setParameter("error-handler", h);
        org.w3c.dom.ls.LSInput in = ls.createLSInput();
        in.setStringData("<r>\n  <1x/>\n</r>");
        try { p.parse(in); } catch (org.w3c.dom.ls.LSException expected) { }
        check(box[0] != null, "DOMLocator available from the DOMError");
        org.w3c.dom.DOMLocator loc = box[0];
        check(loc.getLineNumber() >= 2, "line number points at the offending line");
        check(loc.getColumnNumber() >= 1, "column number is 1-based positive");
        check(loc.getUtf16Offset() >= 0, "utf16 offset non-negative when known");
        check(loc.getUri() == null || loc.getUri().length() >= 0, "uri null or a string");
        // byte offset: -1 when the input was character-based
        boolean byteKnown = loc.getByteOffset() != -1;
        check(!byteKnown || loc.getByteOffset() >= 0, "byte offset either -1 (unknown) or non-negative");
        // second error on a single-line document for a fixed-position check
        final org.w3c.dom.DOMLocator[] first = new org.w3c.dom.DOMLocator[]{null};
        org.w3c.dom.DOMErrorHandler h2 = new org.w3c.dom.DOMErrorHandler() {
            public boolean handleError(org.w3c.dom.DOMError error) { if (first[0] == null) first[0] = error.getLocation(); return true; }
        };
        org.w3c.dom.ls.LSParser p2 = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p2.getDomConfig().setParameter("error-handler", h2);
        org.w3c.dom.ls.LSInput in2 = ls.createLSInput();
        in2.setStringData("<r><1x/></r>");
        try { p2.parse(in2); } catch (org.w3c.dom.ls.LSException expected) { }
        check(first[0].getLineNumber() == 1 && first[0].getColumnNumber() == 5, "single-line document: error located at line 1, column 5");
        check(first[0].getUri() == null, "uri null for in-memory string input");
    }
}
