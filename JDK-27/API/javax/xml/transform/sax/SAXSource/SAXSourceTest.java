/*
 * @test
 * @summary javax.xml.transform.sax.SAXSource conformance: white-box structure + black-box behavior.
 * @run main SAXSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXSourceTest {
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

        Class<?> c = Class.forName("javax.xml.transform.sax.SAXSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Source"), "interface javax.xml.transform.Source");
        check(hasMethod(c, "getInputSource"), "method getInputSource");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getXMLReader"), "method getXMLReader");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setInputSource"), "method setInputSource");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasMethod(c, "setXMLReader"), "method setXMLReader");
        check(hasMethod(c, "sourceToInputSource"), "method sourceToInputSource");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.transform.sax.SAXSource s = new javax.xml.transform.sax.SAXSource();
        check(s.getInputSource() == null, "inputSource null");
        check(s.isEmpty(), "isEmpty");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.transform.sax.SAXSource s = new javax.xml.transform.sax.SAXSource();
        check(s.getInputSource() == null && s.getXMLReader() == null, "no-arg empty");
        org.xml.sax.InputSource is = new org.xml.sax.InputSource(new java.io.StringReader("<r/>"));
        javax.xml.transform.sax.SAXSource s2 = new javax.xml.transform.sax.SAXSource(is);
        check(s2.getInputSource() == is, "InputSource ctor");
        s2.setXMLReader(null); check(s2.getXMLReader() == null, "xml reader null");
        s2.setSystemId("sys"); check("sys".equals(s2.getSystemId()), "setSystemId");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorMatrix();
        testSourceToInputSource();
        testIdentityWithExplicitReader();
        testIsEmptyMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: constructor matrix + sourceToInputSource + transform through explicit XMLReader
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item></root>";

    static void testCtorMatrix() {
        org.xml.sax.InputSource is = new org.xml.sax.InputSource(new java.io.StringReader(DOC));
        javax.xml.transform.sax.SAXSource s1 = new javax.xml.transform.sax.SAXSource(is);
        check(s1.getInputSource() == is, "(InputSource) ctor round-trip");
        check(s1.getXMLReader() == null, "(InputSource) ctor: no reader yet");
        javax.xml.transform.sax.SAXSource s2 = new javax.xml.transform.sax.SAXSource();
        check(s2.getInputSource() == null && s2.getXMLReader() == null, "no-arg ctor empty");
        s2.setSystemId("http://base/x.xml");
        check("http://base/x.xml".equals(s2.getSystemId()), "setSystemId round-trip");
        check("http://javax.xml.transform.sax.SAXSource/feature"
                  .equals(javax.xml.transform.sax.SAXSource.FEATURE),
              "SAXSource.FEATURE constant value");
    }

    static void testSourceToInputSource() {
        javax.xml.transform.stream.StreamSource withReader =
            new javax.xml.transform.stream.StreamSource(
                new java.io.StringReader(DOC), "http://base/x.xml");
        org.xml.sax.InputSource conv1 =
            javax.xml.transform.sax.SAXSource.sourceToInputSource(withReader);
        check("http://base/x.xml".equals(conv1.getSystemId()),
              "sourceToInputSource(StreamSource w/ reader): systemId kept");
        check(conv1.getCharacterStream() != null,
              "sourceToInputSource(StreamSource w/ reader): character stream carried");
        javax.xml.transform.stream.StreamSource withStream =
            new javax.xml.transform.stream.StreamSource(
                new java.io.ByteArrayInputStream(DOC.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        org.xml.sax.InputSource conv2 =
            javax.xml.transform.sax.SAXSource.sourceToInputSource(withStream);
        check(conv2.getByteStream() != null,
              "sourceToInputSource(StreamSource w/ stream): byte stream carried");
    }

    static void testIdentityWithExplicitReader() throws Exception {
        org.xml.sax.XMLReader xr =
            javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        javax.xml.transform.sax.SAXSource src =
            new javax.xml.transform.sax.SAXSource(xr,
                new org.xml.sax.InputSource(new java.io.StringReader(DOC)));
        check(src.getXMLReader() == xr, "(XMLReader,InputSource) ctor keeps the reader");
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        id.transform(src, new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals(DOC),
              "identity transform drives the caller-supplied XMLReader (output exact)");
    }

    static void testIsEmptyMatrix() {
        // JDK-27: isEmpty() true iff no InputSource (and no systemId) set
        javax.xml.transform.sax.SAXSource s = new javax.xml.transform.sax.SAXSource();
        check(s.isEmpty(), "SAXSource no-arg isEmpty=true");
        s.setInputSource(new org.xml.sax.InputSource(new java.io.StringReader(DOC)));
        check(!s.isEmpty(), "SAXSource with InputSource isEmpty=false");
        s.setInputSource(null);
        check(s.isEmpty(), "setInputSource(null) makes it empty again");
    }
}
