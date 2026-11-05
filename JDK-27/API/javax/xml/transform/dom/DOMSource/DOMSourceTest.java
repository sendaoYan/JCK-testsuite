/*
 * @test
 * @summary javax.xml.transform.dom.DOMSource conformance: white-box structure + black-box behavior.
 * @run main DOMSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMSourceTest {
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

        Class<?> c = Class.forName("javax.xml.transform.dom.DOMSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Source"), "interface javax.xml.transform.Source");
        check(hasMethod(c, "getNode"), "method getNode");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setNode"), "method setNode");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.transform.dom.DOMSource s = new javax.xml.transform.dom.DOMSource();
        check(s.getNode() == null, "node null");
        check(s.isEmpty(), "isEmpty");
        s.setSystemId("sys");
        check("sys".equals(s.getSystemId()), "systemId");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = db.parse(new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8")));
        javax.xml.transform.dom.DOMSource s = new javax.xml.transform.dom.DOMSource(d);
        check(s.getNode() == d, "node round-trip");
        javax.xml.transform.dom.DOMSource s2 = new javax.xml.transform.dom.DOMSource();
        check(s2.getNode() == null && s2.getSystemId() == null, "no-arg empty");
        org.w3c.dom.Node n = d.getDocumentElement();
        javax.xml.transform.dom.DOMSource s3 = new javax.xml.transform.dom.DOMSource(n, "sys");
        check(s3.getNode() == n && "sys".equals(s3.getSystemId()), "node + systemId ctor");
        s3.setSystemId("s2"); check("s2".equals(s3.getSystemId()), "setSystemId");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorMatrix();
        testIdentityClosure();
        testIsEmptyMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: constructor matrix + identity transform closure + isEmpty semantics
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item></root>";

    static void testCtorMatrix() throws Exception {
        javax.xml.parsers.DocumentBuilder db =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(new java.io.ByteArrayInputStream(DOC.getBytes("UTF-8")));
        javax.xml.transform.dom.DOMSource s1 = new javax.xml.transform.dom.DOMSource(doc);
        check(s1.getNode() == doc, "(Document) ctor node round-trip");
        check(s1.getSystemId() == null, "(Document) ctor systemId null");
        org.w3c.dom.Element el = doc.getDocumentElement();
        javax.xml.transform.dom.DOMSource s2 =
            new javax.xml.transform.dom.DOMSource(el, "http://base/el");
        check(s2.getNode() == el, "(Node,systemId) ctor node round-trip");
        check("http://base/el".equals(s2.getSystemId()), "(Node,systemId) ctor systemId round-trip");
        javax.xml.transform.dom.DOMSource s3 = new javax.xml.transform.dom.DOMSource();
        check(s3.getNode() == null && s3.getSystemId() == null, "no-arg ctor empty");
        s3.setNode(el);
        check(s3.getNode() == el, "setNode/getNode round-trip");
        s3.setSystemId("sid2");
        check("sid2".equals(s3.getSystemId()), "setSystemId overrides");
        check("http://javax.xml.transform.dom.DOMSource/feature"
                  .equals(javax.xml.transform.dom.DOMSource.FEATURE),
              "DOMSource.FEATURE constant value");
    }

    static void testIdentityClosure() throws Exception {
        javax.xml.parsers.DocumentBuilder db =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(new java.io.ByteArrayInputStream(DOC.getBytes("UTF-8")));
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        id.transform(new javax.xml.transform.dom.DOMSource(doc),
                     new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals(DOC),
              "identity DOMSource->StreamResult reproduces the document exactly");
        javax.xml.transform.dom.DOMSource sub =
            new javax.xml.transform.dom.DOMSource(doc.getDocumentElement());
        java.io.StringWriter sw2 = new java.io.StringWriter();
        id.transform(sub, new javax.xml.transform.stream.StreamResult(sw2));
        check(sw2.toString().replace("\r", "").contains("first"),
              "DOMSource over element subtree transforms the subtree");
    }

    static void testIsEmptyMatrix() throws Exception {
        // JDK-27: isEmpty() true iff nothing set or the node has no content
        javax.xml.transform.dom.DOMSource empty = new javax.xml.transform.dom.DOMSource();
        check(empty.isEmpty(), "empty DOMSource isEmpty=true");
        javax.xml.parsers.DocumentBuilder db =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        javax.xml.transform.dom.DOMSource blank = new javax.xml.transform.dom.DOMSource(db.newDocument());
        check(blank.isEmpty(), "DOMSource over content-less newDocument isEmpty=true");
        org.w3c.dom.Document doc = db.parse(new java.io.ByteArrayInputStream(DOC.getBytes("UTF-8")));
        javax.xml.transform.dom.DOMSource full = new javax.xml.transform.dom.DOMSource(doc);
        check(!full.isEmpty(), "DOMSource over parsed document isEmpty=false");
        full.setNode(null);
        check(full.isEmpty(), "setNode(null) makes it empty again");
    }
}
