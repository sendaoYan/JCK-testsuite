/*
 * @test
 * @summary org.xml.sax.Parser conformance: interface hierarchy and methods.
 * @run main ParserTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ParserTest {
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
        Class<?> c = Class.forName("org.xml.sax.Parser");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setLocale"), "has method setLocale");
        check(names.contains("setEntityResolver"), "has method setEntityResolver");
        check(names.contains("setDTDHandler"), "has method setDTDHandler");
        check(names.contains("setDocumentHandler"), "has method setDocumentHandler");
        check(names.contains("setErrorHandler"), "has method setErrorHandler");
        check(names.contains("parse"), "has method parse");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Parser contract (SAX1 interface via JAXP) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.Parser p = spf.newSAXParser().getParser();
        check(p != null && org.xml.sax.Parser.class.isInstance(p), "getParser() returns a SAX1 Parser");
        // handler wiring round-trips
        org.xml.sax.HandlerBase hb = new org.xml.sax.HandlerBase();
        p.setDocumentHandler(hb);
        p.setErrorHandler(hb);
        p.setEntityResolver(hb);
        p.setDTDHandler(hb);
        check(true, "four SAX1 handler setters accept the same HandlerBase");
        // parse(InputSource) and parse(systemId) forms both parse
        final java.util.List<String> roots = new java.util.ArrayList<String>();
        p.setDocumentHandler(new org.xml.sax.HandlerBase() {
            public void startElement(String name, org.xml.sax.AttributeList a) { roots.add(name); }
        });
        p.parse(new org.xml.sax.InputSource(new java.io.StringReader("<one/>")));
        check(roots.get(0).equals("one"), "parse(InputSource) drives the document");
        java.io.File tmp = java.io.File.createTempFile("sax1", ".xml");
        tmp.deleteOnExit();
        java.io.PrintWriter pw = new java.io.PrintWriter(tmp, "UTF-8");
        pw.print("<two/>"); pw.close();
        p.parse(tmp.toURI().toString());
        check(roots.get(1).equals("two"), "parse(systemId string) drives the document");
        // null input
        try { p.parse((org.xml.sax.InputSource) null); check(false, "parse(null) must throw"); }
        catch (NullPointerException ex) { check(true, "parse(null InputSource) -> NullPointerException"); }
        // setLocale exists on the interface contract (may reject unsupported locales)
        boolean rejected = false;
        try { p.setLocale(java.util.Locale.FRENCH); } catch (org.xml.sax.SAXException ex) { rejected = true; }
        check(true, "setLocale call survived (rejected=" + rejected + " is implementation-dependent)");
    }
}
