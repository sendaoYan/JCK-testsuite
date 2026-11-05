/*
 * @test
 * @summary javax.xml.transform.stream.StreamSource conformance: white-box structure + black-box behavior.
 * @run main StreamSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StreamSourceTest {
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

        Class<?> c = Class.forName("javax.xml.transform.stream.StreamSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Source"), "interface javax.xml.transform.Source");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getReader"), "method getReader");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setInputStream"), "method setInputStream");
        check(hasMethod(c, "setPublicId"), "method setPublicId");
        check(hasMethod(c, "setReader"), "method setReader");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.transform.stream.StreamSource s = new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<a/>"));
        check(s.getReader() != null, "getReader");
        check(!s.isEmpty(), "not empty");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8"));
        javax.xml.transform.stream.StreamSource s = new javax.xml.transform.stream.StreamSource(in);
        check(s.getInputStream() == in, "input stream round-trip");
        java.io.StringReader sr = new java.io.StringReader("<r/>");
        javax.xml.transform.stream.StreamSource s2 = new javax.xml.transform.stream.StreamSource(sr);
        check(s2.getReader() == sr, "reader round-trip");
        javax.xml.transform.stream.StreamSource s3 = new javax.xml.transform.stream.StreamSource("sys");
        check("sys".equals(s3.getSystemId()), "systemId ctor");
        javax.xml.transform.stream.StreamSource s4 = new javax.xml.transform.stream.StreamSource(in, "sys");
        check(s4.getInputStream() == in && "sys".equals(s4.getSystemId()), "stream + systemId ctor");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorFamily();
        testTransformFromFile();
        testIsEmptyMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: full constructor family + File-based transform closure + isEmpty
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item></root>";

    static void testCtorFamily() {
        java.io.ByteArrayInputStream in =
            new java.io.ByteArrayInputStream(DOC.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        javax.xml.transform.stream.StreamSource s1 =
            new javax.xml.transform.stream.StreamSource(in);
        check(s1.getInputStream() == in, "(InputStream) ctor round-trip");
        javax.xml.transform.stream.StreamSource s2 =
            new javax.xml.transform.stream.StreamSource(in, "http://base/a.xml");
        check(s2.getInputStream() == in && "http://base/a.xml".equals(s2.getSystemId()),
              "(InputStream,systemId) ctor");
        java.io.StringReader rd = new java.io.StringReader(DOC);
        javax.xml.transform.stream.StreamSource s3 =
            new javax.xml.transform.stream.StreamSource(rd, "http://base/b.xml");
        check(s3.getReader() == rd && "http://base/b.xml".equals(s3.getSystemId()),
              "(Reader,systemId) ctor");
        javax.xml.transform.stream.StreamSource s4 =
            new javax.xml.transform.stream.StreamSource("http://base/c.xml");
        check("http://base/c.xml".equals(s4.getSystemId()), "(systemId) ctor");
        check(s4.getInputStream() == null && s4.getReader() == null && s4.getPublicId() == null,
              "(systemId) ctor leaves streams null");
        javax.xml.transform.stream.StreamSource s5 =
            new javax.xml.transform.stream.StreamSource();
        java.io.StringReader rd2 = new java.io.StringReader(DOC);
        s5.setReader(rd2);
        check(s5.getReader() == rd2, "setReader/getReader round-trip");
        java.io.ByteArrayInputStream in2 =
            new java.io.ByteArrayInputStream(new byte[]{'<','a','/','>'});
        s5.setInputStream(in2);
        check(s5.getInputStream() == in2, "setInputStream/getInputStream round-trip");
        check("http://javax.xml.transform.stream.StreamSource/feature"
                  .equals(javax.xml.transform.stream.StreamSource.FEATURE),
              "StreamSource.FEATURE constant value");
    }

    static void testTransformFromFile() throws Exception {
        java.io.File tmp = java.io.File.createTempFile("stsrc", ".xml");
        try {
            java.io.FileOutputStream fo = new java.io.FileOutputStream(tmp);
            fo.write(DOC.getBytes("UTF-8"));
            fo.close();
            // (File) ctor derives a systemId from the absolute path
            javax.xml.transform.stream.StreamSource fs =
                new javax.xml.transform.stream.StreamSource(tmp);
            check(fs.getSystemId() != null && fs.getSystemId().endsWith(".xml"),
                  "(File) ctor derives systemId from the file path");
            javax.xml.transform.Transformer id =
                javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            id.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            java.io.StringWriter sw = new java.io.StringWriter();
            id.transform(fs, new javax.xml.transform.stream.StreamResult(sw));
            check(sw.toString().replace("\r", "").equals(DOC),
                  "identity transform reads a File-based StreamSource (output exact)");
        } finally {
            tmp.delete();
        }
    }

    static void testIsEmptyMatrix() {
        // JDK-27: isEmpty() true until any stream/reader/systemId is set
        javax.xml.transform.stream.StreamSource s =
            new javax.xml.transform.stream.StreamSource();
        check(s.isEmpty(), "no-arg StreamSource isEmpty=true");
        s.setSystemId("http://x");
        check(!s.isEmpty(), "setSystemId makes it non-empty");
        javax.xml.transform.stream.StreamSource withReader =
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC));
        check(!withReader.isEmpty(), "StreamSource holding a reader isEmpty=false");
    }
}
