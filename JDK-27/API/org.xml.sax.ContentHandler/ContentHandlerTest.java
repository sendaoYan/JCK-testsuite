/*
 * @test
 * @summary org.xml.sax.ContentHandler conformance: white-box structure + black-box behavior.
 * @run main ContentHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ContentHandlerTest {
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

        Class<?> c = Class.forName("org.xml.sax.ContentHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "characters"), "method characters");
        check(hasMethod(c, "declaration"), "method declaration");
        check(hasMethod(c, "endDocument"), "method endDocument");
        check(hasMethod(c, "endElement"), "method endElement");
        check(hasMethod(c, "endPrefixMapping"), "method endPrefixMapping");
        check(hasMethod(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(hasMethod(c, "processingInstruction"), "method processingInstruction");
        check(hasMethod(c, "setDocumentLocator"), "method setDocumentLocator");
        check(hasMethod(c, "skippedEntity"), "method skippedEntity");
        check(hasMethod(c, "startDocument"), "method startDocument");
        check(hasMethod(c, "startElement"), "method startElement");
        check(hasMethod(c, "startPrefixMapping"), "method startPrefixMapping");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.ContentHandler a = new org.xml.sax.ContentHandler(){
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
        check(a != null, "anon impl of ContentHandler");
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
