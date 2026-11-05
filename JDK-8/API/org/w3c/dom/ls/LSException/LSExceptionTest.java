/*
 * @test
 * @summary org.w3c.dom.ls.LSException conformance: superclass, constructor, code field.
 * @run main LSExceptionTest
 */

public class LSExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(RuntimeException.class.isAssignableFrom(org.w3c.dom.ls.LSException.class),
              "extends RuntimeException");
        check(org.w3c.dom.ls.LSException.PARSE_ERR == org.w3c.dom.ls.LSException.PARSE_ERR, "PARSE_ERR field");
        check(org.w3c.dom.ls.LSException.SERIALIZE_ERR == org.w3c.dom.ls.LSException.SERIALIZE_ERR, "SERIALIZE_ERR field");
        org.w3c.dom.ls.LSException e = new org.w3c.dom.ls.LSException(org.w3c.dom.ls.LSException.PARSE_ERR, "m");
        check(e.code == org.w3c.dom.ls.LSException.PARSE_ERR, "code field");
        check("m".equals(e.getMessage()), "message");

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
