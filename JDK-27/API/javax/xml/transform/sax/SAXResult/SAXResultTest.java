/*
 * @test
 * @summary javax.xml.transform.sax.SAXResult conformance: white-box structure + black-box behavior.
 * @run main SAXResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXResultTest {
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

        Class<?> c = Class.forName("javax.xml.transform.sax.SAXResult");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Result"), "interface javax.xml.transform.Result");
        check(hasMethod(c, "getHandler"), "method getHandler");
        check(hasMethod(c, "getLexicalHandler"), "method getLexicalHandler");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "setHandler"), "method setHandler");
        check(hasMethod(c, "setLexicalHandler"), "method setLexicalHandler");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.transform.sax.SAXResult r = new javax.xml.transform.sax.SAXResult();
        check(r.getHandler() == null, "handler null");
        org.xml.sax.helpers.DefaultHandler h = new org.xml.sax.helpers.DefaultHandler();
        r.setHandler(h);
        check(r.getHandler() == h, "setHandler");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.transform.sax.SAXResult r = new javax.xml.transform.sax.SAXResult();
        check(r.getHandler() == null, "no-arg empty");
        r.setSystemId("sys"); check("sys".equals(r.getSystemId()), "setSystemId");
        r.setLexicalHandler(null); check(r.getLexicalHandler() == null, "lexical handler null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorMatrix();
        testTransformToSaxResult();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: constructor matrix + real transform->SAXResult event closure
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static void testCtorMatrix() {
        org.xml.sax.ContentHandler h = new org.xml.sax.helpers.DefaultHandler();
        javax.xml.transform.sax.SAXResult r =
            new javax.xml.transform.sax.SAXResult(h);
        check(r.getHandler() == h, "(ContentHandler) ctor round-trip");
        check(r.getSystemId() == null, "(ContentHandler) ctor systemId null");
        org.xml.sax.ext.LexicalHandler lh = new org.xml.sax.ext.DefaultHandler2();
        r.setLexicalHandler(lh);
        check(r.getLexicalHandler() == lh, "setLexicalHandler/getLexicalHandler round-trip");
        org.xml.sax.ContentHandler h2 = new org.xml.sax.helpers.DefaultHandler();
        r.setHandler(h2);
        check(r.getHandler() == h2, "setHandler/getHandler round-trip");
        check("http://javax.xml.transform.sax.SAXResult/feature"
                  .equals(javax.xml.transform.sax.SAXResult.FEATURE),
              "SAXResult.FEATURE constant value");
    }

    static void testTransformToSaxResult() throws Exception {
        final java.util.List<String> ev = new java.util.ArrayList<String>();
        org.xml.sax.helpers.DefaultHandler rec = new org.xml.sax.helpers.DefaultHandler() {
            public void startDocument() { ev.add("SD"); }
            public void endDocument() { ev.add("ED"); }
            public void startElement(String u, String l, String q, org.xml.sax.Attributes at) {
                ev.add("SE:" + q + "[" + at.getValue("id") + "]");
            }
            public void endElement(String u, String l, String q) { ev.add("EE:" + q); }
            public void characters(char[] ch, int s, int len) {
                ev.add("CH:" + new String(ch, s, len));
            }
        };
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                     new javax.xml.transform.sax.SAXResult(rec));
        java.util.List<String> expected = java.util.Arrays.asList(
            "SD", "SE:root[null]", "SE:item[3]", "CH:third", "EE:item",
            "SE:item[1]", "CH:first", "EE:item",
            "SE:item[2]", "CH:second", "EE:item", "EE:root", "ED");
        check(ev.equals(expected), "identity transform into SAXResult delivers full SAX event sequence");
    }
}
