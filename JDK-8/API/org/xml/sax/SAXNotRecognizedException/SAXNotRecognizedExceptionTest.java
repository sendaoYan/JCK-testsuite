/*
 * @test
 * @summary org.xml.sax.SAXNotRecognizedException conformance: exception hierarchy and constructors.
 * @run main SAXNotRecognizedExceptionTest
 */


public class SAXNotRecognizedExceptionTest {
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
        check(org.xml.sax.SAXNotRecognizedException.class.getSuperclass().getName().equals("org.xml.sax.SAXException"), "extends org.xml.sax.SAXException");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral SAXNotRecognizedException contract (real trigger) ----
    static void deepContract() throws Exception {
        org.xml.sax.SAXNotRecognizedException e = new org.xml.sax.SAXNotRecognizedException("no such id");
        check("no such id".equals(e.getMessage()), "(String) constructor keeps the message");
        check(org.xml.sax.SAXException.class.isAssignableFrom(org.xml.sax.SAXNotRecognizedException.class), "extends SAXException");
        check(new org.xml.sax.SAXNotRecognizedException().getMessage() == null, "no-arg constructor: null message");
        // real trigger: unknown feature id on a live reader
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        try { xr.setFeature("http://xml.org/sax/features/no-such-feature", true); check(false, "unknown feature must throw"); }
        catch (org.xml.sax.SAXNotRecognizedException ex) { check(ex.getMessage() != null, "setFeature(unknown) -> SAXNotRecognizedException with message"); }
        try { xr.getFeature("http://xml.org/sax/features/no-such-feature"); check(false, "unknown feature get must throw"); }
        catch (org.xml.sax.SAXNotRecognizedException ex) { check(true, "getFeature(unknown) -> SAXNotRecognizedException"); }
        try { xr.setProperty("http://xml.org/sax/properties/no-such-property", new Object()); check(false, "unknown property must throw"); }
        catch (org.xml.sax.SAXNotRecognizedException ex) { check(true, "setProperty(unknown) -> SAXNotRecognizedException"); }
    }
}
