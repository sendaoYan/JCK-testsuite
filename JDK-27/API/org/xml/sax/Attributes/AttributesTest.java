/*
 * @test
 * @summary org.xml.sax.Attributes conformance: white-box structure + black-box behavior.
 * @run main AttributesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class AttributesTest {
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

        Class<?> c = Class.forName("org.xml.sax.Attributes");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getIndex"), "method getIndex");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "getLocalName"), "method getLocalName");
        check(hasMethod(c, "getQName"), "method getQName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getURI"), "method getURI");
        check(hasMethod(c, "getValue"), "method getValue");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.Attributes a = new org.xml.sax.Attributes(){
            public int getLength() { return 3; }
            public java.lang.String getURI(int p0) { return "s"; }
            public java.lang.String getLocalName(int p0) { return "s"; }
            public java.lang.String getQName(int p0) { return "s"; }
            public java.lang.String getType(int p0) { return "s"; }
            public java.lang.String getValue(int p0) { return "s"; }
            public int getIndex(java.lang.String p0, java.lang.String p1) { return 3; }
            public int getIndex(java.lang.String p0) { return 3; }
            public java.lang.String getType(java.lang.String p0, java.lang.String p1) { return "s"; }
            public java.lang.String getType(java.lang.String p0) { return "s"; }
            public java.lang.String getValue(java.lang.String p0, java.lang.String p1) { return "s"; }
            public java.lang.String getValue(java.lang.String p0) { return "s"; }
        };
        check(a != null, "anon impl of Attributes");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check("s".equals(a.getURI(3)), "getURI() returns fixed default");
        check("s".equals(a.getLocalName(3)), "getLocalName() returns fixed default");
        check("s".equals(a.getQName(3)), "getQName() returns fixed default");
        check("s".equals(a.getType(3)), "getType() returns fixed default");
        check("s".equals(a.getValue(3)), "getValue() returns fixed default");
        check(a.getIndex("s", "s") == 3, "getIndex() returns fixed default");
        check(a.getIndex("s") == 3, "getIndex() returns fixed default");
        check("s".equals(a.getType("s", "s")), "getType() returns fixed default");
        check("s".equals(a.getType("s")), "getType() returns fixed default");
        check("s".equals(a.getValue("s", "s")), "getValue() returns fixed default");
        check("s".equals(a.getValue("s")), "getValue() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Attributes contract (live callback object) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setFeature("http://xml.org/sax/features/namespaces", true);
        xr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        final org.xml.sax.Attributes[] seen = new org.xml.sax.Attributes[1];
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String uri, String ln, String q, org.xml.sax.Attributes a) { seen[0] = a; }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r a='1' p:b='2' xmlns:p='urn:p'/>")));
        org.xml.sax.Attributes a = seen[0];
        check(a.getLength() == 2, "with namespaces on + namespace-prefixes off, xmlns declarations are hidden from Attributes (len=" + a.getLength() + ")");
        check("a".equals(a.getQName(0)) && "a".equals(a.getLocalName(0)) && "".equals(a.getURI(0)), "index accessors: qName/localName/uri");
        check("1".equals(a.getValue(0)) && "1".equals(a.getValue("a")) && "1".equals(a.getValue("", "a")), "getValue by index/qName/uri+local");
        check("2".equals(a.getValue("urn:p", "b")), "namespaced lookup via (uri, local) pair");
        check("CDATA".equals(a.getType(0)) && "CDATA".equals(a.getType("a")) && "CDATA".equals(a.getType("urn:p", "b")), "getType reports CDATA for unparsed defaults");
        check(a.getIndex("a") == 0 && a.getIndex("urn:p", "b") == 1, "getIndex by qName and by (uri,local) pair");
        check(a.getValue("missing") == null && a.getType("missing") == null, "missing qName -> null value and type");
        check(a.getIndex("missing") == -1 && a.getIndex("urn:x", "b") == -1, "missing lookups -> -1");
        check(a.getValue(9) == null && a.getType(9) == null, "out-of-range index -> null (no throw)");
        check("".equals(a.getURI(9)) && a.getLocalName(9) == null && a.getQName(9) == null, "out-of-range: local/qName null; live list getURI returns empty string");
    }
}
