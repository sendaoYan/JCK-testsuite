/*
 * @test
 * @summary javax.xml.transform.sax.TransformerHandler conformance: white-box structure + black-box behavior.
 * @run main TransformerHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformerHandlerTest {
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

        Class<?> c = Class.forName("javax.xml.transform.sax.TransformerHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(hasInterface(c, "org.xml.sax.ext.LexicalHandler"), "interface org.xml.sax.ext.LexicalHandler");
        check(hasInterface(c, "org.xml.sax.DTDHandler"), "interface org.xml.sax.DTDHandler");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getTransformer"), "method getTransformer");
        check(hasMethod(c, "setResult"), "method setResult");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.transform.sax.TransformerHandler a = new javax.xml.transform.sax.TransformerHandler(){
            public void setResult(javax.xml.transform.Result p0) { }
            public void setSystemId(java.lang.String p0) { }
            public java.lang.String getSystemId() { return "s"; }
            public javax.xml.transform.Transformer getTransformer() { return null; }
            public void setDocumentLocator(org.xml.sax.Locator p0) { }
            public void startDocument() { }
            public void endDocument() { }
            public void startPrefixMapping(java.lang.String p0, java.lang.String p1) { }
            public void endPrefixMapping(java.lang.String p0) { }
            public void startElement(java.lang.String p0, java.lang.String p1, java.lang.String p2, org.xml.sax.Attributes p3) { }
            public void endElement(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void characters(char[] p0, int p1, int p2) { }
            public void ignorableWhitespace(char[] p0, int p1, int p2) { }
            public void processingInstruction(java.lang.String p0, java.lang.String p1) { }
            public void skippedEntity(java.lang.String p0) { }
            public void startDTD(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void endDTD() { }
            public void startEntity(java.lang.String p0) { }
            public void endEntity(java.lang.String p0) { }
            public void startCDATA() { }
            public void endCDATA() { }
            public void comment(char[] p0, int p1, int p2) { }
            public void notationDecl(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void unparsedEntityDecl(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) { }
        };
        check(a != null, "anon impl of TransformerHandler");
        a.setResult((javax.xml.transform.Result) null);
        a.setSystemId("s");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        check(a.getTransformer() == null, "getTransformer() returns fixed default");
        a.setDocumentLocator((org.xml.sax.Locator) null);
        a.startDocument();
        a.endDocument();
        a.startPrefixMapping("s", "s");
        a.endPrefixMapping("s");
        a.startElement("s", "s", "s", (org.xml.sax.Attributes) null);
        a.endElement("s", "s", "s");
        a.characters((char[]) null, 3, 3);
        a.ignorableWhitespace((char[]) null, 3, 3);
        a.processingInstruction("s", "s");
        a.skippedEntity("s");
        a.startDTD("s", "s", "s");
        a.endDTD();
        a.startEntity("s");
        a.endEntity("s");
        a.startCDATA();
        a.endCDATA();
        a.comment((char[]) null, 3, 3);
        a.notationDecl("s", "s", "s");
        a.unparsedEntityDecl("s", "s", "s", "s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPipelineClosure();
        testParamInjectionAndSystemId();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: real SAX-producer -> TransformerHandler -> Result pipeline
    static final String XSL_ITEM =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:template match=\"item\"><u id=\"{@id}\"/></xsl:template>"
      + "</xsl:stylesheet>";

    static String norm(String s) { return s.replace("\r", ""); }

    static javax.xml.transform.sax.TransformerHandler handler(javax.xml.transform.Source xsl)
            throws Exception {
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        return stf.newTransformerHandler(xsl);
    }

    static void testPipelineClosure() throws Exception {
        // handler built from a compiled Templates
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Templates tpl = stf.newTemplates(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSL_ITEM)));
        javax.xml.transform.sax.TransformerHandler th = stf.newTransformerHandler(tpl);
        java.io.StringWriter sw = new java.io.StringWriter();
        th.setResult(new javax.xml.transform.stream.StreamResult(sw));
        org.xml.sax.helpers.AttributesImpl noAt = new org.xml.sax.helpers.AttributesImpl();
        org.xml.sax.helpers.AttributesImpl id9 = new org.xml.sax.helpers.AttributesImpl();
        id9.addAttribute("", "id", "id", "CDATA", "9");
        org.xml.sax.helpers.AttributesImpl id7 = new org.xml.sax.helpers.AttributesImpl();
        id7.addAttribute("", "id", "id", "CDATA", "7");
        th.startDocument();
        th.startElement("", "root", "root", noAt);
        th.startElement("", "item", "item", id9);
        th.endElement("", "item", "item");
        th.startElement("", "item", "item", id7);
        th.endElement("", "item", "item");
        th.endElement("", "root", "root");
        th.endDocument();
        check(norm(sw.toString()).endsWith("<u id=\"9\"/><u id=\"7\"/>"),
              "SAX events fed to handler(Templates) transform to exact output");
        check(th.getTransformer() != null, "getTransformer non-null");
        // handler built from a Source directly
        javax.xml.transform.sax.TransformerHandler th2 =
            handler(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSL_ITEM)));
        java.io.StringWriter sw2 = new java.io.StringWriter();
        th2.setResult(new javax.xml.transform.stream.StreamResult(sw2));
        org.xml.sax.helpers.AttributesImpl id5 = new org.xml.sax.helpers.AttributesImpl();
        id5.addAttribute("", "id", "id", "CDATA", "5");
        th2.startDocument();
        th2.startElement("", "item", "item", id5);
        th2.endElement("", "item", "item");
        th2.endDocument();
        check(norm(sw2.toString()).endsWith("<u id=\"5\"/>"),
              "SAX events fed to handler(Source) transform to exact output");
    }

    static void testParamInjectionAndSystemId() throws Exception {
        javax.xml.transform.sax.TransformerHandler th = handler(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:param name=\"tag\"/><xsl:template match=\"/\"><w p=\"{$tag}\"/></xsl:template>"
              + "</xsl:stylesheet>")));
        java.io.StringWriter sw = new java.io.StringWriter();
        th.setResult(new javax.xml.transform.stream.StreamResult(sw));
        th.getTransformer().setParameter("tag", "injected");
        th.startDocument();
        th.startElement("", "root", "root", new org.xml.sax.helpers.AttributesImpl());
        th.endElement("", "root", "root");
        th.endDocument();
        check(norm(sw.toString()).endsWith("<w p=\"injected\"/>"),
              "setParameter on handler's transformer drives $param in stylesheet");
        th.setSystemId("http://h/sys");
        check("http://h/sys".equals(th.getSystemId()), "setSystemId/getSystemId round-trip");
        // after a completed run, setResult may be swapped and the handler reused
        java.io.StringWriter sw2 = new java.io.StringWriter();
        th.setResult(new javax.xml.transform.stream.StreamResult(sw2));
        th.startDocument();
        th.startElement("", "root", "root", new org.xml.sax.helpers.AttributesImpl());
        th.endElement("", "root", "root");
        th.endDocument();
        check(norm(sw2.toString()).endsWith("<w p=\"injected\"/>"),
              "handler reusable with a new Result after a completed run");
    }
}
