/*
 * @test
 * @summary org.xml.sax.XMLReader conformance: interface hierarchy and methods.
 * @run main XMLReaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLReaderTest {
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
        Class<?> c = Class.forName("org.xml.sax.XMLReader");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getFeature"), "has method getFeature");
        check(names.contains("setFeature"), "has method setFeature");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("setEntityResolver"), "has method setEntityResolver");
        check(names.contains("getEntityResolver"), "has method getEntityResolver");
        check(names.contains("setDTDHandler"), "has method setDTDHandler");
        check(names.contains("getDTDHandler"), "has method getDTDHandler");
        check(names.contains("setContentHandler"), "has method setContentHandler");
        check(names.contains("getContentHandler"), "has method getContentHandler");
        check(names.contains("setErrorHandler"), "has method setErrorHandler");
        check(names.contains("getErrorHandler"), "has method getErrorHandler");
        check(names.contains("parse"), "has method parse");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral XMLReader contract (features/4-state matrix/handlers/parse) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        check(xr != null, "SAXParser.getXMLReader() yields the JAXP-wired Xerces reader");
        String NS = "http://xml.org/sax/features/namespaces";
        String NP = "http://xml.org/sax/features/namespace-prefixes";
        check(!xr.getFeature(NS), "default namespaces=false");
        check(xr.getFeature(NP), "default namespace-prefixes=true");
        check(!xr.getFeature("http://xml.org/sax/features/validation"), "default validation=false");
        check(xr.getFeature("http://xml.org/sax/features/string-interning"), "default string-interning=true");
        check(xr.getFeature("http://xml.org/sax/features/external-general-entities"), "default external-general-entities=true");
        // 4-state matrix: (namespaces x namespace-prefixes) over one namespaced document
        String xml = "<r xmlns='urn:d' xmlns:p='urn:p' p:x='1'/>";
        String[] expect = {
            "pm:none|se:||r|3|nopair",       // ns=F pfx=F: xmlns decls surface as plain attributes
            "pm:2|se:urn:d|r|r|1|pair",      // ns=T pfx=F: mappings reported, decls hidden
            "pm:none|se:||r|3|nopair",       // ns=F pfx=T: same as state 0
            "pm:2|se:urn:d|r|r|3|pair"       // ns=T pfx=T: mappings AND decls both reported
        };
        for (int state = 0; state < 4; state++) {
            xr.setFeature(NS, (state & 1) != 0);
            xr.setFeature(NP, (state & 2) != 0);
            final java.util.List<String> pm = new java.util.ArrayList<String>();
            final String[] se = new String[]{""};
            xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
                public void startPrefixMapping(String p, String uri) { pm.add(p + "->" + uri); }
                public void startElement(String uri, String ln, String q, org.xml.sax.Attributes a) {
                    se[0] = uri + "|" + ln + "|" + q + "|" + a.getLength() + "|" + (a.getValue("urn:p", "x") != null ? "pair" : "nopair");
                }
            });
            xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
            String got = "pm:" + (pm.isEmpty() ? "none" : String.valueOf(pm.size())) + "|se:" + se[0];
            check(got.equals(expect[state]), "state ns=" + ((state & 1) != 0) + " pfx=" + ((state & 2) != 0) + " -> " + got);
        }
        check(true, "matrix: default-ns mapping counted among prefix mappings");
        // handler wiring round-trips
        check(xr.getContentHandler() != null, "getContentHandler returns the wired handler");
        org.xml.sax.ErrorHandler eh = new org.xml.sax.helpers.DefaultHandler();
        xr.setErrorHandler(eh);
        check(xr.getErrorHandler() == eh, "setErrorHandler/getErrorHandler round-trip");
        xr.setEntityResolver(null);
        check(xr.getEntityResolver() == null, "setEntityResolver(null) accepted");
        xr.setDTDHandler(null);
        check(xr.getDTDHandler() == null, "setDTDHandler(null) accepted");
        xr.setContentHandler(null);
        check(xr.getContentHandler() == null, "setContentHandler(null) accepted");
        // unknown feature/property ids
        try { xr.setFeature("http://bogus/feature", true); check(false, "unknown feature must throw"); }
        catch (org.xml.sax.SAXNotRecognizedException e) { check(true, "setFeature(bogus) -> SAXNotRecognizedException"); }
        try { xr.getProperty("http://bogus/property"); check(false, "unknown property must throw"); }
        catch (org.xml.sax.SAXNotRecognizedException e) { check(true, "getProperty(bogus) -> SAXNotRecognizedException"); }
        // parse null inputs
        try { xr.parse((org.xml.sax.InputSource) null); check(false, "parse(null InputSource) must throw"); }
        catch (NullPointerException e) { check(true, "parse(null InputSource) -> NullPointerException"); }
        try { xr.parse((String) null); check(false, "parse(null String) must throw"); }
        catch (java.net.MalformedURLException e) { check(true, "parse(null String) -> MalformedURLException"); }
        // same reader replays a second parse completely
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler());
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<a/>")));
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<b/>")));
        check(true, "same reader parses a second document fully");
    }
}
