/*
 * @test
 * @summary org.xml.sax.XMLFilter conformance: white-box structure + black-box behavior.
 * @run main XMLFilterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLFilterTest {
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

        Class<?> c = Class.forName("org.xml.sax.XMLFilter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "org.xml.sax.XMLReader"), "interface org.xml.sax.XMLReader");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "setParent"), "method setParent");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.XMLFilter a = new org.xml.sax.XMLFilter(){
            public void setParent(org.xml.sax.XMLReader p0) { }
            public org.xml.sax.XMLReader getParent() { return null; }
            public boolean getFeature(java.lang.String p0) { return true; }
            public void setFeature(java.lang.String p0, boolean p1) { }
            public java.lang.Object getProperty(java.lang.String p0) { return null; }
            public void setProperty(java.lang.String p0, java.lang.Object p1) { }
            public void setEntityResolver(org.xml.sax.EntityResolver p0) { }
            public org.xml.sax.EntityResolver getEntityResolver() { return null; }
            public void setDTDHandler(org.xml.sax.DTDHandler p0) { }
            public org.xml.sax.DTDHandler getDTDHandler() { return null; }
            public void setContentHandler(org.xml.sax.ContentHandler p0) { }
            public org.xml.sax.ContentHandler getContentHandler() { return null; }
            public void setErrorHandler(org.xml.sax.ErrorHandler p0) { }
            public org.xml.sax.ErrorHandler getErrorHandler() { return null; }
            public void parse(org.xml.sax.InputSource p0) { }
            public void parse(java.lang.String p0) { }
        };
        check(a != null, "anon impl of XMLFilter");
        a.setParent((org.xml.sax.XMLReader) null);
        check(a.getParent() == null, "getParent() returns fixed default");
        check(a.getFeature("s") == true, "getFeature() returns fixed default");
        a.setFeature("s", true);
        check(a.getProperty("s") == null, "getProperty() returns fixed default");
        a.setProperty("s", (java.lang.Object) null);
        a.setEntityResolver((org.xml.sax.EntityResolver) null);
        check(a.getEntityResolver() == null, "getEntityResolver() returns fixed default");
        a.setDTDHandler((org.xml.sax.DTDHandler) null);
        check(a.getDTDHandler() == null, "getDTDHandler() returns fixed default");
        a.setContentHandler((org.xml.sax.ContentHandler) null);
        check(a.getContentHandler() == null, "getContentHandler() returns fixed default");
        a.setErrorHandler((org.xml.sax.ErrorHandler) null);
        check(a.getErrorHandler() == null, "getErrorHandler() returns fixed default");
        a.parse((org.xml.sax.InputSource) null);
        a.parse("s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral XMLFilter contract (parent chaining) ----
    static void deepContract() throws Exception {
        check(org.xml.sax.XMLReader.class.isAssignableFrom(org.xml.sax.XMLFilter.class), "XMLFilter extends XMLReader");
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader parent = spf.newSAXParser().getXMLReader();
        org.xml.sax.helpers.XMLFilterImpl filter = new org.xml.sax.helpers.XMLFilterImpl(parent);
        check(filter.getParent() == parent, "constructor wires the parent reader");
        org.xml.sax.XMLReader other = spf.newSAXParser().getXMLReader();
        filter.setParent(other);
        check(filter.getParent() == other, "setParent/getParent round-trip");
        filter.setParent(parent);
        // filter chain: parsing through the filter drives the parent and delivers events to the filter's handler
        final java.util.List<String> events = new java.util.ArrayList<String>();
        filter.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String l, String q, org.xml.sax.Attributes a) { events.add(q); }
        });
        filter.parse(new org.xml.sax.InputSource(new java.io.StringReader("<chain/>")));
        check(events.size() == 1 && events.get(0).equals("chain"), "events flow parent -> filter -> content handler");
        // no parent: parse refuses
        org.xml.sax.helpers.XMLFilterImpl orphan = new org.xml.sax.helpers.XMLFilterImpl();
        check(orphan.getParent() == null, "no-arg constructor leaves parent null");
        boolean npe = false;
        try { orphan.parse(new org.xml.sax.InputSource(new java.io.StringReader("<x/>"))); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "parse without a parent -> NullPointerException");
    }
}
