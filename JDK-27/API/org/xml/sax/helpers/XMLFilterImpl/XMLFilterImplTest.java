/*
 * @test
 * @summary org.xml.sax.helpers.XMLFilterImpl conformance: white-box structure + black-box behavior.
 * @run main XMLFilterImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLFilterImplTest {
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

        Class<?> c = Class.forName("org.xml.sax.helpers.XMLFilterImpl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.XMLFilter"), "interface org.xml.sax.XMLFilter");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "interface org.xml.sax.EntityResolver");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "interface org.xml.sax.DTDHandler");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(hasInterface(c, "org.xml.sax.ErrorHandler"), "interface org.xml.sax.ErrorHandler");
        check(hasMethod(c, "characters"), "method characters");
        check(hasMethod(c, "endDocument"), "method endDocument");
        check(hasMethod(c, "endElement"), "method endElement");
        check(hasMethod(c, "endPrefixMapping"), "method endPrefixMapping");
        check(hasMethod(c, "error"), "method error");
        check(hasMethod(c, "fatalError"), "method fatalError");
        check(hasMethod(c, "getContentHandler"), "method getContentHandler");
        check(hasMethod(c, "getDTDHandler"), "method getDTDHandler");
        check(hasMethod(c, "getEntityResolver"), "method getEntityResolver");
        check(hasMethod(c, "getErrorHandler"), "method getErrorHandler");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(hasMethod(c, "notationDecl"), "method notationDecl");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "processingInstruction"), "method processingInstruction");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
        check(hasMethod(c, "setContentHandler"), "method setContentHandler");
        check(hasMethod(c, "setDTDHandler"), "method setDTDHandler");
        check(hasMethod(c, "setDocumentLocator"), "method setDocumentLocator");
        check(hasMethod(c, "setEntityResolver"), "method setEntityResolver");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setParent"), "method setParent");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "skippedEntity"), "method skippedEntity");
        check(hasMethod(c, "startDocument"), "method startDocument");
        check(hasMethod(c, "startElement"), "method startElement");
        check(hasMethod(c, "startPrefixMapping"), "method startPrefixMapping");
        check(hasMethod(c, "unparsedEntityDecl"), "method unparsedEntityDecl");
        check(hasMethod(c, "warning"), "method warning");

        org.xml.sax.helpers.XMLFilterImpl f = new org.xml.sax.helpers.XMLFilterImpl();
        check(f.getParent() == null, "parent null");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.helpers.XMLFilterImpl f = new org.xml.sax.helpers.XMLFilterImpl();
        check(f.getParent() == null, "default parent null");
        org.xml.sax.XMLReader parent = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        f.setParent(parent);
        check(f.getParent() == parent, "setParent round-trip");
        f.setContentHandler(null); check(f.getContentHandler() == null, "content handler null");
        f.setErrorHandler(null); check(f.getErrorHandler() == null, "error handler null");
        f.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r/>")));
        check(true, "parse delegates to parent");
        org.xml.sax.helpers.XMLFilterImpl f2 = new org.xml.sax.helpers.XMLFilterImpl(parent);
        check(f2.getParent() == parent, "ctor with parent");
        boolean ok = false;
        try { f.getFeature("http://example.invalid/nope"); }
        catch (org.xml.sax.SAXNotRecognizedException e) { ok = true; }
        check(ok, "unknown feature -> SAXNotRecognizedException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral XMLFilterImpl contract (filter chain + selective event forwarding) ----
    static void deepContract() throws Exception {
        org.xml.sax.XMLReader xr = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        org.xml.sax.helpers.XMLFilterImpl f = new org.xml.sax.helpers.XMLFilterImpl(xr);
        check(f.getParent() == xr, "constructor wires the parent reader");
        check(f.getContentHandler() == null && f.getDTDHandler() == null && f.getErrorHandler() == null && f.getEntityResolver() == null, "all handler slots default to null");
        // event forwarding
        final java.util.List<String> log = new java.util.ArrayList<String>();
        f.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { log.add("se(" + q + ")"); }
            public void characters(char[] c, int s, int n) { log.add("ch(" + new String(c, s, n) + ")"); }
            public void endElement(String u, String ln, String q) { log.add("ee(" + q + ")"); }
        });
        f.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r>txt</r>")));
        check(log.equals(java.util.Arrays.asList("se(r)", "ch(txt)", "ee(r)")), "events forwarded parent -> filter -> handler in order");
        // filter-level entity resolver intercepts before the parent resolves
        final String[] resolved = new String[]{null};
        f.setEntityResolver(new org.xml.sax.EntityResolver() {
            public org.xml.sax.InputSource resolveEntity(String p, String s) { resolved[0] = s; return null; }
        });
        boolean fnf = false;
        try { f.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r SYSTEM 'missing-x.dtd'><r/>"))); }
        catch (java.io.FileNotFoundException e) { fnf = true; }
        check(resolved[0] != null && resolved[0].endsWith("missing-x.dtd"), "filter entity resolver consulted for the parent's DTD (systemId=" + resolved[0] + ")");
        check(fnf, "null from the filter resolver defers to default resolution");
        // selective filtering: drop element 'drop' by qName
        org.xml.sax.helpers.XMLFilterImpl sel = new org.xml.sax.helpers.XMLFilterImpl(xr) {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) throws org.xml.sax.SAXException { if (!"drop".equals(q)) super.startElement(u, ln, q, a); }
            public void endElement(String u, String ln, String q) throws org.xml.sax.SAXException { if (!"drop".equals(q)) super.endElement(u, ln, q); }
        };
        final java.util.List<String> kept = new java.util.ArrayList<String>();
        sel.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { kept.add(q); }
        });
        sel.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r><keep/><drop/></r>")));
        check(kept.equals(java.util.Arrays.asList("r", "keep")), "subclass suppresses selected elements (kept=" + kept + ")");
        // two-level chain
        org.xml.sax.helpers.XMLFilterImpl f1 = new org.xml.sax.helpers.XMLFilterImpl(xr);
        org.xml.sax.helpers.XMLFilterImpl f2 = new org.xml.sax.helpers.XMLFilterImpl(f1);
        final StringBuilder chain = new StringBuilder();
        f2.setContentHandler(new org.xml.sax.helpers.DefaultHandler() { public void characters(char[] c, int s, int n) { chain.append(new String(c, s, n)); } });
        f2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r>chain</r>")));
        check("chain".equals(chain.toString()), "two-level filter chain forwards events end to end");
        // orphan filter: parse -> NPE, feature/property access -> SAXNotRecognizedException
        org.xml.sax.helpers.XMLFilterImpl orphan = new org.xml.sax.helpers.XMLFilterImpl();
        check(orphan.getParent() == null, "no-arg constructor leaves parent null");
        boolean orphanNpe = false;
        try { orphan.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r/>"))); }
        catch (NullPointerException e) { orphanNpe = true; }
        check(orphanNpe, "orphan parse -> NullPointerException");
        boolean snr1 = false, snr2 = false, snr3 = false;
        try { orphan.setFeature("http://xml.org/sax/features/namespaces", true); } catch (org.xml.sax.SAXNotRecognizedException e) { snr1 = true; }
        try { orphan.getFeature("http://xml.org/sax/features/namespaces"); } catch (org.xml.sax.SAXNotRecognizedException e) { snr2 = true; }
        try { orphan.setProperty("http://xml.org/sax/properties/lexical-handler", new Object()); } catch (org.xml.sax.SAXNotRecognizedException e) { snr3 = true; }
        check(snr1 && snr2 && snr3, "orphan feature/property access -> SAXNotRecognizedException");
    }
}
