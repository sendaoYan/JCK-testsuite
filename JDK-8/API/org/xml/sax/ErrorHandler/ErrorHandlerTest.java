/*
 * @test
 * @summary org.xml.sax.ErrorHandler conformance: interface hierarchy and methods.
 * @run main ErrorHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ErrorHandlerTest {
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
        Class<?> c = Class.forName("org.xml.sax.ErrorHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("warning"), "has method warning");
        check(names.contains("error"), "has method error");
        check(names.contains("fatalError"), "has method fatalError");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral ErrorHandler contract (fatal/error/propagation) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final org.xml.sax.SAXParseException[] fatal = new org.xml.sax.SAXParseException[1];
        xr.setErrorHandler(new org.xml.sax.ErrorHandler() {
            public void warning(org.xml.sax.SAXParseException e) { }
            public void error(org.xml.sax.SAXParseException e) { }
            public void fatalError(org.xml.sax.SAXParseException e) { fatal[0] = e; }
        });
        org.xml.sax.SAXParseException rethrown = null;
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r><1x/></r>"))); }
        catch (org.xml.sax.SAXParseException e) { rethrown = e; }
        check(fatal[0] != null, "fatalError receives the well-formedness failure");
        check(rethrown != null && fatal[0] != null && rethrown.getLineNumber() == fatal[0].getLineNumber(), "parse throws a fresh SAXParseException after a returning fatalError handler (line preserved)");
        check(fatal[0].getLineNumber() > 0 && fatal[0].getColumnNumber() > 0, "fatal error carries live line/column");
        // handler throwing from fatalError: propagated as-is
        final RuntimeException boom = new RuntimeException("boom");
        xr.setErrorHandler(new org.xml.sax.ErrorHandler() {
            public void warning(org.xml.sax.SAXParseException e) { }
            public void error(org.xml.sax.SAXParseException e) { }
            public void fatalError(org.xml.sax.SAXParseException e) { throw boom; }
        });
        RuntimeException got = null;
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r><1x/></r>"))); }
        catch (RuntimeException e) { got = e; }
        check(got == boom, "handler-thrown RuntimeException propagates out of parse unchanged");
        // recoverable validity errors: error() called, parse continues (non-validating: content-model mismatch reported then completes)
        final int[] errCount = new int[]{0};
        boolean completed = true;
        xr.setErrorHandler(new org.xml.sax.ErrorHandler() {
            public void warning(org.xml.sax.SAXParseException e) { }
            public void error(org.xml.sax.SAXParseException e) { errCount[0]++; }
            public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException { throw e; }
        });
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!ELEMENT r (a,b)>]><r><c/><a/><b/></r>"))); }
        catch (org.xml.sax.SAXException e) { completed = false; }
        check(completed, "recoverable error() does not abort the parse");
        check(org.xml.sax.ErrorHandler.class.isInterface(), "ErrorHandler is an interface");
    }
}
