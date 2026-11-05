/*
 * @test
 * @summary org.xml.sax.ext.DefaultHandler2 conformance: white-box structure + black-box behavior.
 * @run main DefaultHandler2Test
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DefaultHandler2Test {
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

        Class<?> c = Class.forName("org.xml.sax.ext.DefaultHandler2");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.DefaultHandler"), "superclass org.xml.sax.helpers.DefaultHandler");
        check(hasInterface(c, "org.xml.sax.ext.LexicalHandler"), "interface org.xml.sax.ext.LexicalHandler");
        check(hasInterface(c, "org.xml.sax.ext.DeclHandler"), "interface org.xml.sax.ext.DeclHandler");
        check(hasInterface(c, "org.xml.sax.ext.EntityResolver2"), "interface org.xml.sax.ext.EntityResolver2");
        check(hasMethod(c, "attributeDecl"), "method attributeDecl");
        check(hasMethod(c, "comment"), "method comment");
        check(hasMethod(c, "elementDecl"), "method elementDecl");
        check(hasMethod(c, "endCDATA"), "method endCDATA");
        check(hasMethod(c, "endDTD"), "method endDTD");
        check(hasMethod(c, "endEntity"), "method endEntity");
        check(hasMethod(c, "externalEntityDecl"), "method externalEntityDecl");
        check(hasMethod(c, "getExternalSubset"), "method getExternalSubset");
        check(hasMethod(c, "internalEntityDecl"), "method internalEntityDecl");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
        check(hasMethod(c, "startCDATA"), "method startCDATA");
        check(hasMethod(c, "startDTD"), "method startDTD");
        check(hasMethod(c, "startEntity"), "method startEntity");

        org.xml.sax.ext.DefaultHandler2 h = new org.xml.sax.ext.DefaultHandler2();
        h.startCDATA();
        h.endCDATA();
        h.startDTD("a", "b", "c");
        h.endDTD();
        h.comment(new char[]{'-', '-'}, 0, 2);
        check(true, "no-throw");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.ext.DefaultHandler2 h = new org.xml.sax.ext.DefaultHandler2();
        check(h instanceof org.xml.sax.ext.LexicalHandler && h instanceof org.xml.sax.ext.DeclHandler
              && h instanceof org.xml.sax.ext.EntityResolver2, "3 ext interfaces");
        check(h instanceof org.xml.sax.helpers.DefaultHandler, "extends DefaultHandler");
        final StringBuilder log = new StringBuilder();
        org.xml.sax.ext.DefaultHandler2 hh = new org.xml.sax.ext.DefaultHandler2() {
            public void startCDATA() { log.append("CDATA;"); }
            public void comment(char[] c, int s, int len) { log.append("COMMENT;"); }
        };
        javax.xml.parsers.SAXParserFactory f = javax.xml.parsers.SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        javax.xml.parsers.SAXParser sp = f.newSAXParser();
        sp.setProperty("http://xml.org/sax/properties/lexical-handler", hh);
        sp.parse(new java.io.ByteArrayInputStream("<a><!----><![CDATA[x]]></a>".getBytes("UTF-8")), hh);
        check(log.toString().contains("COMMENT;") && log.toString().contains("CDATA;"), "lexical events, got " + log);

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DefaultHandler2 contract (combined extension base) ----
    static void deepContract() throws Exception {
        org.xml.sax.ext.DefaultHandler2 h2 = new org.xml.sax.ext.DefaultHandler2();
        check(org.xml.sax.helpers.DefaultHandler.class.isInstance(h2), "DefaultHandler2 extends DefaultHandler");
        check(org.xml.sax.ext.LexicalHandler.class.isInstance(h2), "DefaultHandler2 implements LexicalHandler");
        check(org.xml.sax.ext.DeclHandler.class.isInstance(h2), "DefaultHandler2 implements DeclHandler");
        check(org.xml.sax.ext.EntityResolver2.class.isInstance(h2), "DefaultHandler2 implements EntityResolver2");
        check(h2.getExternalSubset("r", null) == null, "getExternalSubset default returns null");
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setContentHandler(h2);
        xr.setProperty("http://xml.org/sax/properties/lexical-handler", h2);
        xr.setProperty("http://xml.org/sax/properties/declaration-handler", h2);
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!ELEMENT r (#PCDATA)><!ATTLIST r a CDATA #IMPLIED>]><r><!--c--></r>")));
        check(true, "bare DefaultHandler2 parses a DTD+comment document silently (all roles no-op)");
        // subclass records comment / startDTD / attributeDecl / startCDATA simultaneously
        final java.util.List<String> log = new java.util.ArrayList<String>();
        org.xml.sax.ext.DefaultHandler2 rec = new org.xml.sax.ext.DefaultHandler2() {
            public void comment(char[] c, int s, int n) { log.add("comment(" + new String(c, s, n) + ")"); }
            public void startDTD(String name, String pid, String sid) { log.add("startDTD(" + name + ")"); }
            public void attributeDecl(String e, String a, String t, String m, String v) { log.add("attDecl(" + a + ")"); }
            public void startCDATA() { log.add("startCDATA"); }
            public void endCDATA() { log.add("endCDATA"); }
        };
        org.xml.sax.XMLReader xr2 = spf.newSAXParser().getXMLReader();
        xr2.setContentHandler(rec);
        xr2.setProperty("http://xml.org/sax/properties/lexical-handler", rec);
        xr2.setProperty("http://xml.org/sax/properties/declaration-handler", rec);
        xr2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!ELEMENT r (#PCDATA)><!ATTLIST r a CDATA #IMPLIED>]><r><!--c--><![CDATA[cd]]></r>")));
        check(log.contains("startDTD(r)"), "one instance receives startDTD via the lexical slot");
        check(log.contains("attDecl(a)"), "same instance receives attributeDecl via the declaration slot");
        check(log.contains("comment(c)"), "same instance receives comments via the lexical slot");
        check(log.contains("startCDATA") && log.contains("endCDATA"), "CDATA section brackets reported via the lexical slot");
        int order = log.indexOf("startDTD(r)");
        check(order < log.indexOf("attDecl(a)") && log.indexOf("attDecl(a)") < log.indexOf("startCDATA"), "event order startDTD -> attributeDecl -> startCDATA preserved");
    }
}
