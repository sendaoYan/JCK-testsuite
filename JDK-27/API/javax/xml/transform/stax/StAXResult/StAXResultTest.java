/*
 * @test
 * @summary javax.xml.transform.stax.StAXResult conformance: white-box structure + black-box behavior.
 * @run main StAXResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StAXResultTest {
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

        Class<?> c = Class.forName("javax.xml.transform.stax.StAXResult");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Result"), "interface javax.xml.transform.Result");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getXMLEventWriter"), "method getXMLEventWriter");
        check(hasMethod(c, "getXMLStreamWriter"), "method getXMLStreamWriter");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.stream.XMLStreamWriter w = javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r = new javax.xml.transform.stax.StAXResult(w);
        check(r.getXMLStreamWriter() == w, "getXMLStreamWriter");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLEventWriter w = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r = new javax.xml.transform.stax.StAXResult(w);
        check(r.getXMLEventWriter() == w, "event writer round-trip");
        boolean sok = false;
        try { r.setSystemId("sys"); }
        catch (UnsupportedOperationException e) { sok = true; }
        check(sok, "setSystemId -> UnsupportedOperationException");
        javax.xml.stream.XMLStreamWriter sw = javax.xml.stream.XMLOutputFactory.newFactory().createXMLStreamWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r2 = new javax.xml.transform.stax.StAXResult(sw);
        check(r2.getXMLStreamWriter() == sw, "stream writer round-trip");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorRoundTrips();
        testTransformIntoStaxResult();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: ctor round-trips + real transform into StAXResult closures
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static void testCtorRoundTrips() throws Exception {
        javax.xml.stream.XMLOutputFactory ofac = javax.xml.stream.XMLOutputFactory.newFactory();
        javax.xml.stream.XMLStreamWriter w =
            ofac.createXMLStreamWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r1 = new javax.xml.transform.stax.StAXResult(w);
        check(r1.getXMLStreamWriter() == w, "(XMLStreamWriter) ctor round-trip");
        check(r1.getXMLEventWriter() == null, "(XMLStreamWriter) ctor: no event writer");
        check(r1.getSystemId() == null, "getSystemId null");
        javax.xml.stream.XMLEventWriter ew =
            ofac.createXMLEventWriter(new java.io.StringWriter());
        javax.xml.transform.stax.StAXResult r2 = new javax.xml.transform.stax.StAXResult(ew);
        check(r2.getXMLEventWriter() == ew, "(XMLEventWriter) ctor round-trip");
        check(r2.getXMLStreamWriter() == null, "(XMLEventWriter) ctor: no stream writer");
        boolean uoe = false;
        try { r1.setSystemId("x"); }
        catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "setSystemId -> UnsupportedOperationException");
        check("http://javax.xml.transform.stax.StAXResult/feature"
                  .equals(javax.xml.transform.stax.StAXResult.FEATURE),
              "StAXResult.FEATURE constant value");
    }

    static void testTransformIntoStaxResult() throws Exception {
        javax.xml.stream.XMLOutputFactory ofac = javax.xml.stream.XMLOutputFactory.newFactory();
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        // Stream -> StAXResult(XMLStreamWriter): writer receives the full document
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter w = ofac.createXMLStreamWriter(sw);
        id.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                     new javax.xml.transform.stax.StAXResult(w));
        check(sw.toString().replace("\r", "").endsWith(DOC),
              "identity Stream->StAXResult(XMLStreamWriter) writes exact document");
        // Stream -> StAXResult(XMLEventWriter)
        java.io.StringWriter sw2 = new java.io.StringWriter();
        javax.xml.stream.XMLEventWriter ew = ofac.createXMLEventWriter(sw2);
        id.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                     new javax.xml.transform.stax.StAXResult(ew));
        String out = sw2.toString().replace("\r", "");
        check(out.contains("<root>") && out.contains("third") && out.endsWith("</root>"),
              "identity Stream->StAXResult(XMLEventWriter) writes document");
    }
}
