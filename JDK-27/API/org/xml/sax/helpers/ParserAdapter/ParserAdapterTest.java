/*
 * @test
 * @summary org.xml.sax.helpers.ParserAdapter conformance: white-box structure + black-box behavior.
 * @run main ParserAdapterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ParserAdapterTest {
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

        Class<?> c = Class.forName("org.xml.sax.helpers.ParserAdapter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.XMLReader"), "interface org.xml.sax.XMLReader");
        check(hasInterface(c, "org.xml.sax.DocumentHandler"), "interface org.xml.sax.DocumentHandler");
        check(hasMethod(c, "characters"), "method characters");
        check(hasMethod(c, "endDocument"), "method endDocument");
        check(hasMethod(c, "endElement"), "method endElement");
        check(hasMethod(c, "getContentHandler"), "method getContentHandler");
        check(hasMethod(c, "getDTDHandler"), "method getDTDHandler");
        check(hasMethod(c, "getEntityResolver"), "method getEntityResolver");
        check(hasMethod(c, "getErrorHandler"), "method getErrorHandler");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "processingInstruction"), "method processingInstruction");
        check(hasMethod(c, "setContentHandler"), "method setContentHandler");
        check(hasMethod(c, "setDTDHandler"), "method setDTDHandler");
        check(hasMethod(c, "setDocumentLocator"), "method setDocumentLocator");
        check(hasMethod(c, "setEntityResolver"), "method setEntityResolver");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "startDocument"), "method startDocument");
        check(hasMethod(c, "startElement"), "method startElement");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.Parser parser = org.xml.sax.helpers.ParserFactory.makeParser("com.sun.org.apache.xerces.internal.parsers.SAXParser");
        org.xml.sax.helpers.ParserAdapter pa = new org.xml.sax.helpers.ParserAdapter(parser);
        check(pa != null, "ctor with Parser");
        pa.setContentHandler(null); check(pa.getContentHandler() == null, "content handler");
        pa.setErrorHandler(null); check(pa.getErrorHandler() == null, "error handler");
        pa.setEntityResolver(null); check(pa.getEntityResolver() == null, "entity resolver");
        check(pa instanceof org.xml.sax.XMLReader, "is XMLReader");
        boolean noarg = false;
        try { new org.xml.sax.helpers.ParserAdapter(); }
        catch (org.xml.sax.SAXException e) { noarg = true; }
        check(noarg, "no-arg ctor -> SAXException without system property");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral ParserAdapter contract (SAX1 -> SAX2 bridging) ----
    static void deepContract() throws Exception {
        boolean npe = false;
        try { new org.xml.sax.helpers.ParserAdapter((org.xml.sax.Parser) null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "constructor with null Parser -> NullPointerException");
        org.xml.sax.Parser sax1 = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getParser();
        org.xml.sax.helpers.ParserAdapter pa = new org.xml.sax.helpers.ParserAdapter(sax1);
        check(org.xml.sax.XMLReader.class.isInstance(pa), "ParserAdapter is an XMLReader");
        check(pa.getFeature("http://xml.org/sax/features/namespaces"), "namespaces feature defaults to true");
        check(!pa.getFeature("http://xml.org/sax/features/namespace-prefixes"), "namespace-prefixes feature defaults to false");
        final java.util.List<String> log = new java.util.ArrayList<String>();
        final org.xml.sax.Locator[] seen = new org.xml.sax.Locator[1];
        pa.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) { seen[0] = l; log.add("loc"); }
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { log.add("se(" + u + "|" + ln + "|" + q + "|len=" + a.getLength() + ")"); }
            public void characters(char[] c, int s, int n) { log.add("ch(" + new String(c, s, n) + ")"); }
        });
        check(pa.getContentHandler() != null, "setContentHandler/getContentHandler round-trip");
        pa.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r xmlns='urn:d' a='1'>t</r>")));
        check(seen[0] != null, "setDocumentLocator forwarded to the SAX2 handler");
        check(log.contains("se(urn:d|r|r|len=1)"), "adapter synthesizes namespace-qualified startElement from SAX1 data");
        check(log.contains("ch(t)"), "character data forwarded");
        // prefix preservation via namespace-prefixes=true
        org.xml.sax.helpers.ParserAdapter pa2 = new org.xml.sax.helpers.ParserAdapter(sax1);
        pa2.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        final String[] qSeen = new String[]{null};
        pa2.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { qSeen[0] = q; }
        });
        pa2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<p:r xmlns:p='urn:p'/>")));
        check("p:r".equals(qSeen[0]), "namespace-prefixes=true keeps the raw qName");
        // non-namespace mode: raw names, empty uri/local
        pa2.setFeature("http://xml.org/sax/features/namespaces", false);
        final String[] rawSeen = new String[]{null};
        pa2.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { rawSeen[0] = u + "|" + ln + "|" + q; }
        });
        pa2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<p:r xmlns:p='urn:p'/>")));
        check("||p:r".equals(rawSeen[0]), "namespaces=false yields empty uri/local with the raw qName (observed: " + rawSeen[0] + ")");
        // DTD/error/entity roles pass through
        org.xml.sax.helpers.ParserAdapter pa3 = new org.xml.sax.helpers.ParserAdapter(sax1);
        org.xml.sax.DTDHandler dh = new org.xml.sax.helpers.DefaultHandler();
        org.xml.sax.ErrorHandler eh = new org.xml.sax.helpers.DefaultHandler();
        pa3.setDTDHandler(dh);
        pa3.setErrorHandler(eh);
        check(pa3.getDTDHandler() == dh && pa3.getErrorHandler() == eh, "DTD/error handler setters round-trip");
        pa3.setEntityResolver(null);
        check(pa3.getEntityResolver() == null, "entity resolver nullable");
    }
}
