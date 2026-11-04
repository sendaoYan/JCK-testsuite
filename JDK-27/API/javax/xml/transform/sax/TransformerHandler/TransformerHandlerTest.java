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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
