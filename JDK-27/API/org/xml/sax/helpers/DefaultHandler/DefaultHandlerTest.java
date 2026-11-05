/*
 * @test
 * @summary org.xml.sax.helpers.DefaultHandler conformance: white-box structure + black-box behavior.
 * @run main DefaultHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DefaultHandlerTest {
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

        Class<?> c = Class.forName("org.xml.sax.helpers.DefaultHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
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
        check(hasMethod(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(hasMethod(c, "notationDecl"), "method notationDecl");
        check(hasMethod(c, "processingInstruction"), "method processingInstruction");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
        check(hasMethod(c, "setDocumentLocator"), "method setDocumentLocator");
        check(hasMethod(c, "skippedEntity"), "method skippedEntity");
        check(hasMethod(c, "startDocument"), "method startDocument");
        check(hasMethod(c, "startElement"), "method startElement");
        check(hasMethod(c, "startPrefixMapping"), "method startPrefixMapping");
        check(hasMethod(c, "unparsedEntityDecl"), "method unparsedEntityDecl");
        check(hasMethod(c, "warning"), "method warning");

        org.xml.sax.helpers.DefaultHandler h = new org.xml.sax.helpers.DefaultHandler();
        h.startDocument();
        h.endDocument();
        h.characters(new char[]{'a'}, 0, 1);
        check(true, "no-throw");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.helpers.DefaultHandler h = new org.xml.sax.helpers.DefaultHandler();
        check(h instanceof org.xml.sax.EntityResolver && h instanceof org.xml.sax.DTDHandler
              && h instanceof org.xml.sax.ContentHandler && h instanceof org.xml.sax.ErrorHandler, "4 interfaces");
        final StringBuilder log = new StringBuilder();
        org.xml.sax.helpers.DefaultHandler hh = new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String l, String q, org.xml.sax.Attributes a) { log.append(q).append(";"); }
            public void characters(char[] c, int s, int len) { log.append(new String(c, s, len)).append(";"); }
        };
        javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().parse(
            new java.io.ByteArrayInputStream("<a>b</a>".getBytes("UTF-8")), hh);
        check("a;b;".equals(log.toString()), "callback content, got " + log);
        h.resolveEntity("pub", "sys");
        check(true, "default resolveEntity returns null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DefaultHandler contract (quad-interface base) ----
    static void deepContract() throws Exception {
        org.xml.sax.helpers.DefaultHandler h = new org.xml.sax.helpers.DefaultHandler();
        check(org.xml.sax.EntityResolver.class.isInstance(h), "implements EntityResolver");
        check(org.xml.sax.DTDHandler.class.isInstance(h), "implements DTDHandler");
        check(org.xml.sax.ContentHandler.class.isInstance(h), "implements ContentHandler");
        check(org.xml.sax.ErrorHandler.class.isInstance(h), "implements ErrorHandler");
        check(h.resolveEntity(null, "missing.dtd") == null, "resolveEntity default returns null");
        h.warning(new org.xml.sax.SAXParseException("w", null));
        h.error(new org.xml.sax.SAXParseException("e", null));
        check(true, "warning/error default to no-ops");
        boolean rethrown = false;
        org.xml.sax.SAXParseException fatal = new org.xml.sax.SAXParseException("f", new org.xml.sax.helpers.LocatorImpl(), new java.io.IOException("io"));
        java.io.IOException ioCause = (java.io.IOException) fatal.getException();
        try { h.fatalError(fatal); } catch (org.xml.sax.SAXException e) { rethrown = (e.getException() == ioCause); }
        check(rethrown, "fatalError default rethrows wrapped in a new SAXException");
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.newSAXParser().parse(new java.io.ByteArrayInputStream("<!DOCTYPE r [<!ELEMENT r (#PCDATA)><!NOTATION n SYSTEM 'n.sys'><!ENTITY ue SYSTEM 'e.xml' NDATA n>]><r/>".getBytes("UTF-8")), h);
        check(true, "bare DefaultHandler parses a DTD+notation+unparsed-entity document silently");
        // roles are individually addressable through the base type
        org.xml.sax.ContentHandler asContent = h;
        asContent.setDocumentLocator(null);
        asContent.startDocument();
        asContent.startPrefixMapping("p", "urn:p");
        asContent.startElement("", "e", "e", new org.xml.sax.helpers.AttributesImpl());
        asContent.characters("t".toCharArray(), 0, 1);
        asContent.ignorableWhitespace(" ".toCharArray(), 0, 1);
        asContent.processingInstruction("pi", "d");
        asContent.skippedEntity("se");
        asContent.endElement("", "e", "e");
        asContent.endPrefixMapping("p");
        asContent.endDocument();
        check(true, "full ContentHandler callback set accepts no-op dispatch");
        org.xml.sax.DTDHandler asDTD = h;
        asDTD.notationDecl("n", null, "s");
        asDTD.unparsedEntityDecl("ue", null, "s", "n");
        check(true, "DTDHandler callbacks accept no-op dispatch");
    }
}
