/*
 * @test
 * @summary org.xml.sax.SAXNotSupportedException conformance: exception hierarchy and constructors.
 * @run main SAXNotSupportedExceptionTest
 */


public class SAXNotSupportedExceptionTest {
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
        check(org.xml.sax.SAXNotSupportedException.class.getSuperclass().getName().equals("org.xml.sax.SAXException"), "extends org.xml.sax.SAXException");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral SAXNotSupportedException contract (real trigger) ----
    static void deepContract() throws Exception {
        org.xml.sax.SAXNotSupportedException e = new org.xml.sax.SAXNotSupportedException("cannot do that");
        check("cannot do that".equals(e.getMessage()), "(String) constructor keeps the message");
        check(org.xml.sax.SAXException.class.isAssignableFrom(org.xml.sax.SAXNotSupportedException.class), "extends SAXException");
        check(!org.xml.sax.SAXNotRecognizedException.class.isInstance(e), "distinct from SAXNotRecognizedException");
        // real trigger: recognized-but-unsupported feature (is-standalone is read-only)
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        try { xr.setFeature("http://xml.org/sax/features/is-standalone", true); check(false, "read-only feature must throw"); }
        catch (org.xml.sax.SAXNotSupportedException ex) { check(true, "setFeature(is-standalone) -> SAXNotSupportedException"); }
        // recognized property with a wrong-typed value
        try { xr.setProperty("http://xml.org/sax/properties/lexical-handler", "not-a-handler"); check(false, "wrong-typed property value must throw"); }
        catch (org.xml.sax.SAXNotSupportedException ex) { check(true, "setProperty(lexical-handler, String) -> SAXNotSupportedException"); }
    }
}
