/*
 * @test
 * @summary org.xml.sax.InputSource conformance: white-box structure + black-box behavior.
 * @run main InputSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class InputSourceTest {
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

        Class<?> c = Class.forName("org.xml.sax.InputSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getByteStream"), "method getByteStream");
        check(hasMethod(c, "getCharacterStream"), "method getCharacterStream");
        check(hasMethod(c, "getEncoding"), "method getEncoding");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setByteStream"), "method setByteStream");
        check(hasMethod(c, "setCharacterStream"), "method setCharacterStream");
        check(hasMethod(c, "setEncoding"), "method setEncoding");
        check(hasMethod(c, "setPublicId"), "method setPublicId");
        check(hasMethod(c, "setSystemId"), "method setSystemId");

        org.xml.sax.InputSource s = new org.xml.sax.InputSource();
        s.setPublicId("p");
        s.setSystemId("s");
        check("p".equals(s.getPublicId()), "publicId");
        check("s".equals(s.getSystemId()), "systemId");
        check(!s.isEmpty(), "not empty");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        org.xml.sax.InputSource is = new org.xml.sax.InputSource();
        check(is.getPublicId() == null && is.getSystemId() == null, "no-arg empty");
        is.setPublicId("pub"); is.setSystemId("sys"); is.setEncoding("UTF-8");
        check("pub".equals(is.getPublicId()) && "sys".equals(is.getSystemId()) && "UTF-8".equals(is.getEncoding()), "setters");
        java.io.ByteArrayInputStream bs = new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8"));
        is.setByteStream(bs);
        check(is.getByteStream() == bs && is.getCharacterStream() == null, "byte stream round-trip");
        check(!is.isEmpty(), "isEmpty false with byte stream");
        java.io.StringReader sr = new java.io.StringReader("<r/>");
        org.xml.sax.InputSource is2 = new org.xml.sax.InputSource(sr);
        check(is2.getCharacterStream() == sr, "reader ctor");
        org.xml.sax.InputSource is3 = new org.xml.sax.InputSource("sys");
        check("sys".equals(is3.getSystemId()), "systemId ctor");
        org.xml.sax.InputSource is4 = new org.xml.sax.InputSource(new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8")));
        check(is4.getByteStream() != null, "stream ctor");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral InputSource contract (constructors + property channels) ----
    static void deepContract() throws Exception {
        org.xml.sax.InputSource e = new org.xml.sax.InputSource();
        check(e.getPublicId() == null && e.getSystemId() == null, "empty constructor: ids null");
        check(e.getByteStream() == null && e.getCharacterStream() == null && e.getEncoding() == null, "empty constructor: streams/encoding null");
        org.xml.sax.InputSource s = new org.xml.sax.InputSource("sys.xml");
        check("sys.xml".equals(s.getSystemId()), "(String) constructor sets systemId");
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream("<a/>".getBytes("UTF-8"));
        org.xml.sax.InputSource b = new org.xml.sax.InputSource(bis);
        check(b.getByteStream() == bis, "(InputStream) constructor sets byteStream");
        java.io.StringReader sr = new java.io.StringReader("<a/>");
        org.xml.sax.InputSource r = new org.xml.sax.InputSource(sr);
        check(r.getCharacterStream() == sr, "(Reader) constructor sets characterStream");
        // setters/getters round-trip, byte and char streams independent
        e.setPublicId("pub"); e.setSystemId("sys2"); e.setEncoding("UTF-16");
        check("pub".equals(e.getPublicId()) && "sys2".equals(e.getSystemId()) && "UTF-16".equals(e.getEncoding()), "id/encoding round-trip");
        e.setByteStream(bis);
        check(e.getByteStream() == bis && e.getCharacterStream() == null, "byteStream set leaves characterStream null");
        e.setCharacterStream(sr);
        check(e.getCharacterStream() == sr && e.getByteStream() == bis, "both streams coexist");
        // real parse through an InputSource carrying a Reader
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final String[] root = new String[]{null};
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String l, String q, org.xml.sax.Attributes a) { if (root[0] == null) root[0] = q; }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<fromReader/>")));
        check("fromReader".equals(root[0]), "parser consumes the InputSource characterStream");
    }
}
