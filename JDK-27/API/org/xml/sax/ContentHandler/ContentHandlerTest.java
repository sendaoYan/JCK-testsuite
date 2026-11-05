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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral ContentHandler contract (full callback order) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setFeature("http://xml.org/sax/features/namespaces", true);
        final java.util.List<String> log = new java.util.ArrayList<String>();
        final org.xml.sax.Locator[] loc = new org.xml.sax.Locator[1];
        final int[] lineAtStart = new int[]{-1};
        xr.setContentHandler(new org.xml.sax.ContentHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) { loc[0] = l; log.add("locator"); }
            public void startDocument() { log.add("startDocument"); }
            public void endDocument() { log.add("endDocument"); }
            public void startPrefixMapping(String p, String u) { log.add("startPM(" + p + ")"); }
            public void endPrefixMapping(String p) { log.add("endPM(" + p + ")"); }
            public void startElement(String uri, String ln, String q, org.xml.sax.Attributes a) { log.add("start(" + ln + ")"); if (lineAtStart[0] < 0 && "r".equals(ln)) lineAtStart[0] = loc[0].getLineNumber(); }
            public void endElement(String uri, String ln, String q) { log.add("end(" + ln + ")"); }
            public void characters(char[] c, int s, int n) { log.add("chars(" + new String(c, s, n) + ")"); }
            public void ignorableWhitespace(char[] c, int s, int n) { log.add("ws(" + n + ")"); }
            public void processingInstruction(String t, String d) { log.add("pi(" + t + ")"); }
            public void skippedEntity(String n) { log.add("skip(" + n + ")"); }
        });
        String xml = "<?pi1 before?><r xmlns:p='urn:p'><?pi2 in?><p:a>T</p:a></r><?pi3 after?>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        check(log.get(0).equals("locator") && log.get(1).equals("startDocument"), "setDocumentLocator precedes startDocument");
        check(log.indexOf("startPM(p)") >= 0 && log.indexOf("startPM(p)") < log.indexOf("start(r)"), "prefix mapping precedes its element");
        check(log.indexOf("pi(pi2)") > log.indexOf("start(r)") && log.indexOf("pi(pi2)") < log.indexOf("start(a)"), "PI inside root reported in document order");
        check(log.get(log.size() - 1).equals("endDocument"), "endDocument is the final callback");
        int iStart = log.indexOf("start(r)");
        int iEnd = log.indexOf("end(r)");
        int iEndPM = log.indexOf("endPM(p)");
        check(iEndPM > iEnd, "endPrefixMapping follows endElement");
        check(log.indexOf("end(a)") > log.indexOf("chars(T)") && log.indexOf("chars(T)") > log.indexOf("start(a)"), "characters between start/end of the element");
        check(log.indexOf("startDocument") < iStart && iEnd < log.indexOf("endDocument"), "elements nested inside document bounds");
        check(loc[0] != null && lineAtStart[0] >= 1, "locator handed to the handler is live (line " + lineAtStart[0] + " at root start)");
        // PI outside the root also reported (before start of root / after end of root)
        check(log.indexOf("pi(pi1)") != -1 && log.indexOf("pi(pi1)") < log.indexOf("startPM(p)") && log.indexOf("pi(pi1)") < iStart, "PI before the root reported before root start");
        check(log.indexOf("pi(pi3)") > iEnd && log.indexOf("pi(pi3)") < log.indexOf("endDocument"), "PI after the root reported after root end");
        check(log.indexOf("endPM(p)") > log.indexOf("end(r)") && log.indexOf("endPM(p)") < log.indexOf("pi(pi3)"), "endPrefixMapping after endElement, before trailing PI");
        // characters may split; concatenation preserves content
        final StringBuilder text = new StringBuilder();
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void characters(char[] c, int s, int n) { text.append(c, s, n); }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r>0123456789012345678901234567890123456789012345678901234567890123456789</r>")));
        check(text.length() == 70, "chunked characters concatenate to the full text (len=" + text.length() + ")");
        // skippedEntity via external-general-entities=false
        xr.setFeature("http://xml.org/sax/features/external-general-entities", false);
        final java.util.List<String> skipped = new java.util.ArrayList<String>();
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void skippedEntity(String n) { skipped.add(n); }
        });
        String sk = "<!DOCTYPE r [<!ENTITY sk SYSTEM 'sk.txt'>]><r>&sk;</r>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(sk)));
        check(skipped.size() == 1 && skipped.get(0).equals("sk"), "skippedEntity reports the unreplaced entity and parse completes");
        // ignorableWhitespace needs an element content model
        final int[] wsLen = new int[]{-1};
        xr.setFeature("http://xml.org/sax/features/external-general-entities", true);
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void ignorableWhitespace(char[] c, int s, int n) { wsLen[0] = n; }
        });
        String ics = "<!DOCTYPE r [<!ELEMENT r (a*,b)><!ELEMENT a (#PCDATA)><!ELEMENT b (#PCDATA)>]><r>  <a/>  <b/></r>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(ics)));
        check(wsLen[0] > 0, "inter-element whitespace reported via ignorableWhitespace under a content model");
    }
}
