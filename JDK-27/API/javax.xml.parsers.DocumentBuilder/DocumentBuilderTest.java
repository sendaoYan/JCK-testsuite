/*
 * @test
 * @summary javax.xml.parsers.DocumentBuilder conformance: white-box structure + black-box behavior.
 * @run main DocumentBuilderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DocumentBuilderTest {
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

        Class<?> c = Class.forName("javax.xml.parsers.DocumentBuilder");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getDOMImplementation"), "method getDOMImplementation");
        check(hasMethod(c, "getSchema"), "method getSchema");
        check(hasMethod(c, "isNamespaceAware"), "method isNamespaceAware");
        check(hasMethod(c, "isValidating"), "method isValidating");
        check(hasMethod(c, "isXIncludeAware"), "method isXIncludeAware");
        check(hasMethod(c, "newDocument"), "method newDocument");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setEntityResolver"), "method setEntityResolver");
        check(hasMethod(c, "setErrorHandler"), "method setErrorHandler");

        javax.xml.parsers.DocumentBuilder b = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = b.newDocument();
        check(d != null, "newDocument");
        check(b.getDOMImplementation() != null, "getDOMImplementation");
        check(b.newDocument().getDocumentElement() == null, "empty document");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder db = f.newDocumentBuilder();
        String xml = "<?xml version=\"1.0\"?><catalog><book id=\"b1\" lang=\"en\"><title>T1</title></book></catalog>";
        java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(xml.getBytes("UTF-8"));
        org.w3c.dom.Document d = db.parse(in);
        check("catalog".equals(d.getDocumentElement().getNodeName()), "parse root element name");
        org.w3c.dom.NodeList books = d.getElementsByTagName("book");
        check(books.getLength() == 1, "getElementsByTagName count");
        check("b1".equals(((org.w3c.dom.Element) books.item(0)).getAttribute("id")), "attribute value");
        check("T1".equals(books.item(0).getFirstChild().getTextContent()), "nested text content");
        // parse via InputSource
        org.w3c.dom.Document d2 = db.parse(new org.xml.sax.InputSource(
                new java.io.ByteArrayInputStream(xml.getBytes("UTF-8"))));
        check(d2.getDocumentElement().getNodeName().equals("catalog"), "parse(InputSource)");
        // system-id overload resolves relative URIs deterministically
        org.w3c.dom.Document d3 = db.parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")), "http://example.invalid/doc.xml");
        check(d3.getDocumentElement().getNodeName().equals("catalog"), "parse(InputStream, systemId)");
        // fresh empty document
        org.w3c.dom.Document empty = db.newDocument();
        check(empty.getChildNodes().getLength() == 0 && empty.getDocumentElement() == null, "newDocument empty");
        check(empty.createElement("e") != null, "newDocument usable");
        check(db.getDOMImplementation() != null, "getDOMImplementation");
        check(!db.isNamespaceAware() && !db.isValidating(), "builder configuration queries");
        check(db.getSchema() == null, "no schema by default");
        // malformed XML -> SAXParseException with line/column >= 1
        boolean ok = false;
        try { db.parse(new java.io.ByteArrayInputStream("<a><b></a>".getBytes("UTF-8"))); }
        catch (org.xml.sax.SAXParseException e) { ok = e.getLineNumber() > 0 && e.getColumnNumber() > 0; }
        check(ok, "malformed XML -> SAXParseException with position");
        // reset clears handlers, parser reusable afterwards
        db.setErrorHandler(null); db.setEntityResolver(null); db.reset();
        org.w3c.dom.Document d4 = db.parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        check(d4.getDocumentElement().getNodeName().equals("catalog"), "reusable after reset");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
