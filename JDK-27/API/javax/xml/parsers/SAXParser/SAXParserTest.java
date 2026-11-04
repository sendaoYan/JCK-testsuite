/*
 * @test
 * @summary javax.xml.parsers.SAXParser conformance: white-box structure + black-box behavior.
 * @run main SAXParserTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXParserTest {
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

        Class<?> c = Class.forName("javax.xml.parsers.SAXParser");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getParser"), "method getParser");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getSchema"), "method getSchema");
        check(hasMethod(c, "getXMLReader"), "method getXMLReader");
        check(hasMethod(c, "isNamespaceAware"), "method isNamespaceAware");
        check(hasMethod(c, "isValidating"), "method isValidating");
        check(hasMethod(c, "isXIncludeAware"), "method isXIncludeAware");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setProperty"), "method setProperty");

        javax.xml.parsers.SAXParser p = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
        check(p.getXMLReader() != null, "getXMLReader");
        check(p.getParser() != null, "getParser");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.SAXParserFactory f = javax.xml.parsers.SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser p = f.newSAXParser();
        final StringBuilder log = new StringBuilder();
        final java.util.List<String> atts = new java.util.ArrayList<String>();
        org.xml.sax.helpers.DefaultHandler h = new org.xml.sax.helpers.DefaultHandler() {
            public void startDocument() { log.append("SD;"); }
            public void startElement(String u, String ln, String qn, org.xml.sax.Attributes a) {
                log.append("SE:").append(qn).append(";");
                for (int i = 0; i < a.getLength(); i++) atts.add(a.getQName(i) + "=" + a.getValue(i));
            }
            public void characters(char[] ch, int s, int l) { log.append("CH:").append(new String(ch, s, l)).append(";"); }
            public void endElement(String u, String ln, String qn) { log.append("EE:").append(qn).append(";"); }
            public void endDocument() { log.append("ED;"); }
        };
        String xml = "<msg to=\"you\">hi</msg>";
        p.parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")), h);
        check("SD;SE:msg;CH:hi;EE:msg;ED;".equals(log.toString()), "callback sequence exact, got " + log);
        check(atts.size() == 1 && "to=you".equals(atts.get(0)), "attribute callback content");
        check(p.getXMLReader() instanceof org.xml.sax.XMLReader, "getXMLReader");
        check(p.getParser() instanceof org.xml.sax.Parser, "getParser");
        p.reset();
        log.setLength(0);
        p.parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")), h);
        check("SD;SE:msg;CH:hi;EE:msg;ED;".equals(log.toString()), "parse again after reset");
        // InputSource overload
        log.setLength(0);
        p.parse(new org.xml.sax.InputSource(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8"))), h);
        check(log.toString().startsWith("SD;SE:msg;"), "parse(InputSource, handler)");
        // properties
        boolean ok = false;
        try { p.getProperty("http://example.invalid/nope"); }
        catch (org.xml.sax.SAXNotRecognizedException e) { ok = true; }
        check(ok, "getProperty(unknown) -> SAXNotRecognizedException");
        ok = false;
        try { p.setProperty("http://example.invalid/nope", "v"); }
        catch (org.xml.sax.SAXNotRecognizedException e) { ok = true; }
        check(ok, "setProperty(unknown) -> SAXNotRecognizedException");
        ok = false;
        try { p.parse(new java.io.ByteArrayInputStream("<a>".getBytes("UTF-8")), h); }
        catch (org.xml.sax.SAXParseException e) { ok = true; }
        check(ok, "premature EOF -> SAXParseException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
