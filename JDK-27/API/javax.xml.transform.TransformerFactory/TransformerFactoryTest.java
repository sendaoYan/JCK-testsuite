/*
 * @test
 * @summary javax.xml.transform.TransformerFactory conformance: white-box structure + black-box behavior.
 * @run main TransformerFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformerFactoryTest {
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

        Class<?> c = Class.forName("javax.xml.transform.TransformerFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getAssociatedStylesheet"), "method getAssociatedStylesheet");
        check(hasMethod(c, "getAttribute"), "method getAttribute");
        check(hasMethod(c, "getErrorListener"), "method getErrorListener");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getURIResolver"), "method getURIResolver");
        check(hasMethod(c, "newDefaultInstance"), "method newDefaultInstance");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "newTemplates"), "method newTemplates");
        check(hasMethod(c, "newTransformer"), "method newTransformer");
        check(hasMethod(c, "setAttribute"), "method setAttribute");
        check(hasMethod(c, "setErrorListener"), "method setErrorListener");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setURIResolver"), "method setURIResolver");

        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer t = f.newTransformer();
        check(t != null, "newTransformer");
        java.io.StringWriter out = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<a><b>c</b></a>")), new javax.xml.transform.stream.StreamResult(out));
        check(out.toString().contains("c"), "transform output");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        check(f != null, "newInstance");
        javax.xml.transform.Transformer t = f.newTransformer();
        check(t != null, "newTransformer()");
        // identity transform of a fixed DOM
        javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = db.parse(new java.io.ByteArrayInputStream("<r>v</r>".getBytes("UTF-8")));
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.dom.DOMSource(d), new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().contains("<r>v</r>"), "identity transform preserves content");
        // URI resolver + error listener round-trip
        f.setURIResolver(null); check(f.getURIResolver() == null, "URIResolver null round-trip");
        javax.xml.transform.ErrorListener el = new javax.xml.transform.ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) {}
            public void error(javax.xml.transform.TransformerException e) {}
            public void fatalError(javax.xml.transform.TransformerException e) {}
        };
        f.setErrorListener(el); check(f.getErrorListener() == el, "ErrorListener round-trip");
        // attribute round-trip
        boolean aok = false;
        try { f.setAttribute("x", "y"); }
        catch (IllegalArgumentException e) { aok = true; }
        check(aok, "setAttribute(unknown name) -> IllegalArgumentException");
        // feature
        check(f.getFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING) == true
              || f.getFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING) == false, "feature queryable");
        check(javax.xml.transform.TransformerFactory.newDefaultInstance() != null, "newDefaultInstance");
        // Templates from identity stylesheet
        javax.xml.transform.Templates tmpl = f.newTemplates(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
            "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'><xsl:template match='/'><out/></xsl:template></xsl:stylesheet>")));
        check(tmpl != null, "newTemplates");
        sw = new java.io.StringWriter();
        tmpl.newTransformer().transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<r/>")), new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().contains("<out"), "template produces out element");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
