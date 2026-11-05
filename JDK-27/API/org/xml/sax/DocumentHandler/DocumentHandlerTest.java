/*
 * @test
 * @summary org.xml.sax.DocumentHandler conformance: white-box structure + black-box behavior.
 * @run main DocumentHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DocumentHandlerTest {
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

        Class<?> c = Class.forName("org.xml.sax.DocumentHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "characters"), "method characters");
        check(hasMethod(c, "endDocument"), "method endDocument");
        check(hasMethod(c, "endElement"), "method endElement");
        check(hasMethod(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(hasMethod(c, "processingInstruction"), "method processingInstruction");
        check(hasMethod(c, "setDocumentLocator"), "method setDocumentLocator");
        check(hasMethod(c, "startDocument"), "method startDocument");
        check(hasMethod(c, "startElement"), "method startElement");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.DocumentHandler a = new org.xml.sax.DocumentHandler(){
            public void setDocumentLocator(org.xml.sax.Locator p0) { }
            public void startDocument() { }
            public void endDocument() { }
            public void startElement(java.lang.String p0, org.xml.sax.AttributeList p1) { }
            public void endElement(java.lang.String p0) { }
            public void characters(char[] p0, int p1, int p2) { }
            public void ignorableWhitespace(char[] p0, int p1, int p2) { }
            public void processingInstruction(java.lang.String p0, java.lang.String p1) { }
        };
        check(a != null, "anon impl of DocumentHandler");
        a.setDocumentLocator((org.xml.sax.Locator) null);
        a.startDocument();
        a.endDocument();
        a.startElement("s", (org.xml.sax.AttributeList) null);
        a.endElement("s");
        a.characters((char[]) null, 3, 3);
        a.ignorableWhitespace((char[]) null, 3, 3);
        a.processingInstruction("s", "s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DocumentHandler contract (SAX1 event set) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.Parser p1 = spf.newSAXParser().getParser();
        final java.util.List<String> log = new java.util.ArrayList<String>();
        p1.setDocumentHandler(new org.xml.sax.DocumentHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) { log.add("locator"); }
            public void startDocument() { log.add("start"); }
            public void endDocument() { log.add("end"); }
            public void startElement(String name, org.xml.sax.AttributeList a) { log.add("se(" + name + "#" + a.getLength() + ")"); }
            public void endElement(String name) { log.add("ee(" + name + ")"); }
            public void characters(char[] c, int s, int n) { log.add("ch(" + new String(c, s, n).trim() + ")"); }
            public void ignorableWhitespace(char[] c, int s, int n) { log.add("ws"); }
            public void processingInstruction(String t, String d) { log.add("pi(" + t + ")"); }
        });
        p1.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r x='1'>T</r>")));
        check(log.get(0).equals("locator") && log.get(1).equals("start"), "SAX1 order: locator then startDocument");
        check(log.contains("se(r#1)") && log.contains("ch(T)") && log.contains("ee(r)"), "SAX1 element/characters/end sequence with attribute count");
        check(log.get(log.size() - 1).equals("end"), "endDocument terminates the SAX1 stream");
        check(org.xml.sax.DocumentHandler.class.isInterface(), "DocumentHandler is an interface");
    }
}
