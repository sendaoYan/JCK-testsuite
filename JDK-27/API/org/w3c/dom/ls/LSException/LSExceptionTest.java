/*
 * @test
 * @summary org.w3c.dom.ls.LSException conformance: superclass, constructor, code field.
 * @run main LSExceptionTest
 */

import org.w3c.dom.ls.LSException;

public class LSExceptionTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(RuntimeException.class.isAssignableFrom(LSException.class), "LSException extends RuntimeException");
        check(LSException.PARSE_ERR == 81, "PARSE_ERR == 81");
        check(LSException.SERIALIZE_ERR == 82, "SERIALIZE_ERR == 82");
        LSException e = new LSException(LSException.PARSE_ERR, "boom");
        check(e.code == LSException.PARSE_ERR, "code field");
        check("boom".equals(e.getMessage()), "message");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(LSException.PARSE_ERR == 81, "PARSE_ERR == 81");
        check(LSException.SERIALIZE_ERR == 82, "SERIALIZE_ERR == 82");
        org.w3c.dom.ls.LSException e = new org.w3c.dom.ls.LSException((short) 3, "msg");
        check(e.code == (short) 3, "code field set");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof RuntimeException, "is RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LSException contract ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.ls.LSException.PARSE_ERR == 81, "PARSE_ERR = 81");
        check(org.w3c.dom.ls.LSException.SERIALIZE_ERR == 82, "SERIALIZE_ERR = 82");
        org.w3c.dom.ls.LSException e = new org.w3c.dom.ls.LSException(org.w3c.dom.ls.LSException.PARSE_ERR, "boom");
        check(e.code == org.w3c.dom.ls.LSException.PARSE_ERR, "code is a public field (no getCode())");
        check("boom".equals(e.getMessage()), "message preserved");
        check(RuntimeException.class.isAssignableFrom(org.w3c.dom.ls.LSException.class), "LSException is a RuntimeException");
        e.code = org.w3c.dom.ls.LSException.SERIALIZE_ERR;
        check(e.code == 82, "code field mutable");
        check(new org.w3c.dom.ls.LSException((short) 82, null).getMessage() == null, "null message preserved");
        // real trigger: parse malformed XML without an error handler
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument().getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        org.w3c.dom.ls.LSInput in = ls.createLSInput();
        in.setStringData("<r><1x/></r>");
        try { p.parse(in); check(false, "malformed parse must throw LSException"); }
        catch (org.w3c.dom.ls.LSException ex) { check(ex.code == org.w3c.dom.ls.LSException.PARSE_ERR, "malformed input -> LSException PARSE_ERR(81)"); }
    }
}
