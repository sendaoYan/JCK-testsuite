/*
 * @test
 * @summary javax.xml.transform.stax.StAXSource conformance: white-box structure + black-box behavior.
 * @run main StAXSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StAXSourceTest {
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

        Class<?> c = Class.forName("javax.xml.transform.stax.StAXSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Source"), "interface javax.xml.transform.Source");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getXMLEventReader"), "method getXMLEventReader");
        check(hasMethod(c, "getXMLStreamReader"), "method getXMLStreamReader");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.stream.XMLStreamReader reader = javax.xml.stream.XMLInputFactory.newFactory().createXMLStreamReader(new java.io.StringReader("<a/>"));
        javax.xml.transform.stax.StAXSource s = new javax.xml.transform.stax.StAXSource(reader);
        check(s.getXMLStreamReader() == reader, "getXMLStreamReader");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLEventReader er = javax.xml.stream.XMLInputFactory.newFactory().createXMLEventReader(new java.io.StringReader("<r/>"));
        javax.xml.transform.stax.StAXSource s = new javax.xml.transform.stax.StAXSource(er);
        check(s.getXMLEventReader() == er, "event reader round-trip");
        boolean sok = false;
        try { s.setSystemId("sys"); }
        catch (UnsupportedOperationException e) { sok = true; }
        check(sok, "setSystemId -> UnsupportedOperationException");
        javax.xml.stream.XMLStreamReader sr = javax.xml.stream.XMLInputFactory.newFactory().createXMLStreamReader(new java.io.StringReader("<r/>"));
        javax.xml.transform.stax.StAXSource s2 = new javax.xml.transform.stax.StAXSource(sr);
        check(s2.getXMLStreamReader() == sr, "stream reader round-trip");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorRoundTrips();
        testIdentityBridging();
        testXsltFromStaxSource();
        testIsEmptyMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: ctor round-trips + StAX identity bridging + XSLT closure
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static void testCtorRoundTrips() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r =
            ifac.createXMLStreamReader(new java.io.StringReader(DOC));
        javax.xml.transform.stax.StAXSource s1 = new javax.xml.transform.stax.StAXSource(r);
        check(s1.getXMLStreamReader() == r, "(XMLStreamReader) ctor round-trip");
        check(s1.getXMLEventReader() == null, "(XMLStreamReader) ctor: no event reader");
        check(s1.getSystemId() == null, "getSystemId null (derived from reader location)");
        javax.xml.stream.XMLEventReader er =
            ifac.createXMLEventReader(new java.io.StringReader(DOC));
        javax.xml.transform.stax.StAXSource s2 = new javax.xml.transform.stax.StAXSource(er);
        check(s2.getXMLEventReader() == er, "(XMLEventReader) ctor round-trip");
        check(s2.getXMLStreamReader() == null, "(XMLEventReader) ctor: no stream reader");
        boolean uoe = false;
        try { s1.setSystemId("x"); }
        catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "setSystemId -> UnsupportedOperationException");
        check("http://javax.xml.transform.stax.StAXSource/feature"
                  .equals(javax.xml.transform.stax.StAXSource.FEATURE),
              "StAXSource.FEATURE constant value");
    }

    static void testIdentityBridging() throws Exception {
        // XMLStreamReader -> XMLStreamWriter through identity transform
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLOutputFactory ofac = javax.xml.stream.XMLOutputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r =
            ifac.createXMLStreamReader(new java.io.StringReader(DOC));
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = ofac.createXMLStreamWriter(sw);
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id.transform(new javax.xml.transform.stax.StAXSource(r),
                     new javax.xml.transform.stax.StAXResult(w));
        check(sw.toString().replace("\r", "").endsWith(DOC),
              "identity transform bridges StAX reader -> StAX writer (document intact)");
        check(sw.toString().startsWith("<?xml"), "StAX bridged output carries a declaration");
    }

    static void testXsltFromStaxSource() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.transform.Transformer t =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer(
                new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                    "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                  + "<xsl:template match=\"/\"><out><xsl:value-of select=\"root/item[2]/@name\"/></out>"
                  + "</xsl:template></xsl:stylesheet>")));
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        javax.xml.stream.XMLStreamReader r =
            ifac.createXMLStreamReader(new java.io.StringReader(DOC));
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stax.StAXSource(r),
                    new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals("<out>alpha</out>"),
              "XSLT transform reading from StAXSource (output exact)");
    }

    static void testIsEmptyMatrix() throws Exception {
        // JDK-27: StAXSource.isEmpty() false whenever it holds a reader
        javax.xml.stream.XMLStreamReader r = javax.xml.stream.XMLInputFactory.newFactory()
            .createXMLStreamReader(new java.io.StringReader(DOC));
        javax.xml.transform.stax.StAXSource s = new javax.xml.transform.stax.StAXSource(r);
        check(!s.isEmpty(), "StAXSource holding an XMLStreamReader isEmpty=false");
    }
}
