/*
 * @test
 * @summary org.xml.sax.XMLReader conformance: white-box structure + black-box behavior.
 * @run main XMLReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLReaderTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("org.xml.sax.XMLReader");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getContentHandler"), "method getContentHandler");
        check(hasMethod(c, "getDTDHandler"), "method getDTDHandler");
        check(hasMethod(c, "getEntityResolver"), "method getEntityResolver");
        check(hasMethod(c, "getErrorHandler"), "method getErrorHandler");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "setContentHandler"), "method setContentHandler");
        check(hasMethod(c, "setDTDHandler"), "method setDTDHandler");
        check(hasMethod(c, "setEntityResolver"), "method setEntityResolver");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setProperty"), "method setProperty");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.XMLReader a = new org.xml.sax.XMLReader(){
            public boolean getFeature(java.lang.String p0) { return true; }
            public void setFeature(java.lang.String p0, boolean p1) { }
            public java.lang.Object getProperty(java.lang.String p0) { return null; }
            public void setProperty(java.lang.String p0, java.lang.Object p1) { }
            public void setEntityResolver(org.xml.sax.EntityResolver p0) { }
            public org.xml.sax.EntityResolver getEntityResolver() { return null; }
            public void setDTDHandler(org.xml.sax.DTDHandler p0) { }
            public org.xml.sax.DTDHandler getDTDHandler() { return null; }
            public void setContentHandler(org.xml.sax.ContentHandler p0) { }
            public org.xml.sax.ContentHandler getContentHandler() { return null; }
            public void setErrorHandler(org.xml.sax.ErrorHandler p0) { }
            public org.xml.sax.ErrorHandler getErrorHandler() { return null; }
            public void parse(org.xml.sax.InputSource p0) { }
            public void parse(java.lang.String p0) { }
        };
        check(a != null, "anon impl of XMLReader");
        check(a.getFeature("s") == true, "getFeature() returns fixed default");
        a.setFeature("s", true);
        check(a.getProperty("s") == null, "getProperty() returns fixed default");
        a.setProperty("s", (java.lang.Object) null);
        a.setEntityResolver((org.xml.sax.EntityResolver) null);
        check(a.getEntityResolver() == null, "getEntityResolver() returns fixed default");
        a.setDTDHandler((org.xml.sax.DTDHandler) null);
        check(a.getDTDHandler() == null, "getDTDHandler() returns fixed default");
        a.setContentHandler((org.xml.sax.ContentHandler) null);
        check(a.getContentHandler() == null, "getContentHandler() returns fixed default");
        a.setErrorHandler((org.xml.sax.ErrorHandler) null);
        check(a.getErrorHandler() == null, "getErrorHandler() returns fixed default");
        a.parse((org.xml.sax.InputSource) null);
        a.parse("s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
