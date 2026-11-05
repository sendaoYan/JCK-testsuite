/*
 * @test
 * @summary javax.xml.transform.stream.StreamResult conformance: white-box structure + black-box behavior.
 * @run main StreamResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StreamResultTest {
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

        Class<?> c = Class.forName("javax.xml.transform.stream.StreamResult");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Result"), "interface javax.xml.transform.Result");
        check(hasMethod(c, "getOutputStream"), "method getOutputStream");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getWriter"), "method getWriter");
        check(hasMethod(c, "setOutputStream"), "method setOutputStream");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasMethod(c, "setWriter"), "method setWriter");
        check(hasField(c, "FEATURE"), "field FEATURE");

        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.stream.StreamResult r = new javax.xml.transform.stream.StreamResult(sw);
        check(r.getWriter() == sw, "getWriter");
        check(r.getSystemId() == null, "systemId null");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.transform.stream.StreamResult r = new javax.xml.transform.stream.StreamResult(bos);
        check(r.getOutputStream() == bos, "output stream round-trip");
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.stream.StreamResult r2 = new javax.xml.transform.stream.StreamResult(sw);
        check(r2.getWriter() == sw, "writer round-trip");
        javax.xml.transform.stream.StreamResult r3 = new javax.xml.transform.stream.StreamResult("file.xml");
        check("file.xml".equals(r3.getSystemId()), "systemId ctor");
        r3.setSystemId("s"); check("s".equals(r3.getSystemId()), "setSystemId");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorFamily();
        testTransformClosures();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: full constructor family + real transform closures
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item></root>";

    static void testCtorFamily() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.transform.stream.StreamResult r1 =
            new javax.xml.transform.stream.StreamResult(bos);
        check(r1.getOutputStream() == bos, "(OutputStream) ctor round-trip");
        check(r1.getSystemId() == null, "(OutputStream) ctor systemId null");
        java.io.StringWriter w = new java.io.StringWriter();
        javax.xml.transform.stream.StreamResult r3 =
            new javax.xml.transform.stream.StreamResult(w);
        check(r3.getWriter() == w, "(Writer) ctor round-trip");
        r3.setSystemId("http://base/w");
        check(r3.getWriter() == w && "http://base/w".equals(r3.getSystemId()),
              "systemId independent of the writer sink");
        javax.xml.transform.stream.StreamResult r4 =
            new javax.xml.transform.stream.StreamResult("sysid-only");
        check("sysid-only".equals(r4.getSystemId()), "(systemId) ctor");
        check(r4.getOutputStream() == null && r4.getWriter() == null,
              "(systemId) ctor leaves sinks null");
        java.io.File tmp = java.io.File.createTempFile("stres", ".xml");
        try {
            javax.xml.transform.stream.StreamResult r5 =
                new javax.xml.transform.stream.StreamResult(tmp);
            check(r5.getSystemId() != null && r5.getSystemId().endsWith(".xml"),
                  "(File) ctor derives systemId from the file path");
            javax.xml.transform.stream.StreamResult r6 =
                new javax.xml.transform.stream.StreamResult();
            r6.setSystemId(tmp);
            check(r6.getSystemId() != null && r6.getSystemId().endsWith(".xml"),
                  "setSystemId(File) derives file URL");
        } finally {
            tmp.delete();
        }
        javax.xml.transform.stream.StreamResult r7 =
            new javax.xml.transform.stream.StreamResult();
        java.io.StringWriter w2 = new java.io.StringWriter();
        r7.setWriter(w2);
        check(r7.getWriter() == w2, "setWriter/getWriter round-trip");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        r7.setOutputStream(bos2);
        check(r7.getOutputStream() == bos2, "setOutputStream/getOutputStream round-trip");
        check("http://javax.xml.transform.stream.StreamResult/feature"
                  .equals(javax.xml.transform.stream.StreamResult.FEATURE),
              "StreamResult.FEATURE constant value");
    }

    static void testTransformClosures() throws Exception {
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        // to OutputStream: UTF-8 bytes decode to the exact document
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        id.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        id.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                     new javax.xml.transform.stream.StreamResult(bos));
        check(new String(bos.toByteArray(), "UTF-8").replace("\r", "").equals(DOC),
              "identity transform to StreamResult(OutputStream): exact UTF-8 bytes");
        // to Writer
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.Transformer id2 =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id2.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        id2.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                      new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals(DOC),
              "identity transform to StreamResult(Writer): exact characters");
    }
}
