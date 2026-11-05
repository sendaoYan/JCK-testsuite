/*
 * @test
 * @summary org.xml.sax.HandlerBase conformance: white-box structure + black-box behavior.
 * @run main HandlerBaseTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class HandlerBaseTest {
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

        Class<?> c = Class.forName("org.xml.sax.HandlerBase");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "interface org.xml.sax.EntityResolver");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "interface org.xml.sax.DTDHandler");
        check(hasInterface(c, "org.xml.sax.DocumentHandler"), "interface org.xml.sax.DocumentHandler");
        check(hasInterface(c, "org.xml.sax.ErrorHandler"), "interface org.xml.sax.ErrorHandler");
        check(hasMethod(c, "characters"), "method characters");
        check(hasMethod(c, "endDocument"), "method endDocument");
        check(hasMethod(c, "endElement"), "method endElement");
        check(hasMethod(c, "error"), "method error");
        check(hasMethod(c, "fatalError"), "method fatalError");
        check(hasMethod(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(hasMethod(c, "notationDecl"), "method notationDecl");
        check(hasMethod(c, "processingInstruction"), "method processingInstruction");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
        check(hasMethod(c, "setDocumentLocator"), "method setDocumentLocator");
        check(hasMethod(c, "startDocument"), "method startDocument");
        check(hasMethod(c, "startElement"), "method startElement");
        check(hasMethod(c, "unparsedEntityDecl"), "method unparsedEntityDecl");
        check(hasMethod(c, "warning"), "method warning");

        org.xml.sax.HandlerBase h = new org.xml.sax.HandlerBase();
        h.startDocument();
        h.endDocument();
        check(true, "no-throw");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.HandlerBase h = new org.xml.sax.HandlerBase();
        check(h != null, "instantiable");
        javax.xml.parsers.SAXParser p = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
        // HandlerBase implements the legacy 4 handler interfaces
        p.parse(new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8")), h);
        check(true, "HandlerBase usable as SAX handler");
        check(h instanceof org.xml.sax.EntityResolver && h instanceof org.xml.sax.DTDHandler
              && h instanceof org.xml.sax.DocumentHandler && h instanceof org.xml.sax.ErrorHandler, "4 interfaces");
        h.resolveEntity("pub", "sys");
        check(true, "resolveEntity callable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral HandlerBase contract (SAX1 default base) ----
    static void deepContract() throws Exception {
        org.xml.sax.HandlerBase hb = new org.xml.sax.HandlerBase();
        check(org.xml.sax.DocumentHandler.class.isInstance(hb), "HandlerBase implements DocumentHandler");
        check(org.xml.sax.EntityResolver.class.isInstance(hb), "HandlerBase implements EntityResolver");
        check(org.xml.sax.DTDHandler.class.isInstance(hb), "HandlerBase implements DTDHandler");
        check(org.xml.sax.ErrorHandler.class.isInstance(hb), "HandlerBase implements ErrorHandler");
        check(hb.resolveEntity("pub", "sys") == null, "default resolveEntity returns null (default resolution)");
        // all overridables are no-ops: a full parse through the bare base completes silently
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.Parser p1 = spf.newSAXParser().getParser();
        p1.setDocumentHandler(hb);
        p1.setErrorHandler(hb);
        p1.setEntityResolver(hb);
        p1.setDTDHandler(hb);
        p1.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!NOTATION n SYSTEM 'x'><!ENTITY u SYSTEM 'y' NDATA n>]><r a='1'>T<?pi x?></r>")));
        check(true, "bare HandlerBase swallows a full document (all four roles no-op)");
        // subclass recording still receives events
        final java.util.List<String> got = new java.util.ArrayList<String>();
        org.xml.sax.HandlerBase rec = new org.xml.sax.HandlerBase() {
            public void startElement(String name, org.xml.sax.AttributeList a) { got.add(name + "#" + a.getLength()); }
            public void characters(char[] c, int s, int n) { got.add(new String(c, s, n)); }
        };
        org.xml.sax.Parser p2 = spf.newSAXParser().getParser();
        p2.setDocumentHandler(rec);
        p2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r x='1'>T</r>")));
        check(got.contains("r#1") && got.contains("T"), "subclass overrides receive the SAX1 events");
    }
}
