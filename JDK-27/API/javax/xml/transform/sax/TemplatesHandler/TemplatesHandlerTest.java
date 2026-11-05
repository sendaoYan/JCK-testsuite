/*
 * @test
 * @summary javax.xml.transform.sax.TemplatesHandler conformance: white-box structure + black-box behavior.
 * @run main TemplatesHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TemplatesHandlerTest {
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

        Class<?> c = Class.forName("javax.xml.transform.sax.TemplatesHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getTemplates"), "method getTemplates");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.transform.sax.TemplatesHandler a = new javax.xml.transform.sax.TemplatesHandler(){
            public javax.xml.transform.Templates getTemplates() { return null; }
            public void setSystemId(java.lang.String p0) { }
            public java.lang.String getSystemId() { return "s"; }
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
        };
        check(a != null, "anon impl of TemplatesHandler");
        check(a.getTemplates() == null, "getTemplates() returns fixed default");
        a.setSystemId("s");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCompileClosure();
        testSystemIdLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: real SAX-fed stylesheet compilation closure
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static final String XSL_ITEM =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:template match=\"item\"><u id=\"{@id}\"/></xsl:template>"
      + "</xsl:stylesheet>";

    static javax.xml.transform.sax.TemplatesHandler feed(String xsl) throws Exception {
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.sax.TemplatesHandler thh = stf.newTemplatesHandler();
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setNamespaceAware(true); // required so xsl: elements arrive with their namespace
        org.xml.sax.XMLReader pr = spf.newSAXParser().getXMLReader();
        pr.setContentHandler(thh);
        pr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xsl)));
        return thh;
    }

    static void testCompileClosure() throws Exception {
        javax.xml.transform.sax.TemplatesHandler thh = feed(XSL_ITEM);
        javax.xml.transform.Templates tpl = thh.getTemplates();
        check(tpl != null, "getTemplates non-null after SAX-fed stylesheet parse");
        javax.xml.transform.Transformer t = tpl.newTransformer();
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                    new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals("<u id=\"3\"/><u id=\"1\"/><u id=\"2\"/>"),
              "compiled Templates produce exact output");
        // non-stylesheet input: handler still yields a (default-rules) Templates object;
        // recorded per-javap-confirmed behavior on both JDK-8 and JDK-27
        javax.xml.transform.sax.TemplatesHandler bad = feed("<not-a-stylesheet/>");
        check(bad.getTemplates() != null,
              "non-stylesheet SAX input -> getTemplates still non-null (default-rules templates)");
    }

    static void testSystemIdLifecycle() throws Exception {
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.sax.TemplatesHandler thh = stf.newTemplatesHandler();
        thh.setSystemId("http://base/main.xsl");
        check("http://base/main.xsl".equals(thh.getSystemId()),
              "setSystemId/getSystemId round-trip before parse");
        // parsing with a systemId-bearing InputSource records it on the handler
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        org.xml.sax.XMLReader pr = spf.newSAXParser().getXMLReader();
        pr.setContentHandler(thh);
        org.xml.sax.InputSource is =
            new org.xml.sax.InputSource(new java.io.StringReader(XSL_ITEM));
        is.setSystemId("http://base/auto.xsl");
        pr.parse(is);
        check("http://base/auto.xsl".equals(thh.getSystemId()),
              "handler systemId auto-recorded from parsed InputSource");
        check(thh.getTemplates() != null, "stylesheet compiled after systemId parse");
    }
}
