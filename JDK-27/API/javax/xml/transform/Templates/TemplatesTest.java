/*
 * @test
 * @summary javax.xml.transform.Templates conformance: white-box structure + black-box behavior.
 * @run main TemplatesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TemplatesTest {
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

        Class<?> c = Class.forName("javax.xml.transform.Templates");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getOutputProperties"), "method getOutputProperties");
        check(hasMethod(c, "newTransformer"), "method newTransformer");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.transform.Templates a = new javax.xml.transform.Templates(){
            public javax.xml.transform.Transformer newTransformer() { return null; }
            public java.util.Properties getOutputProperties() { return null; }
        };
        check(a != null, "anon impl of Templates");
        check(a.newTransformer() == null, "newTransformer() returns fixed default");
        check(a.getOutputProperties() == null, "getOutputProperties() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReuseIsolation();
        testOutputPropertiesSnapshot();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== deepening: real Templates closures =====================

    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static final String XSL_PARAM =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:param name=\"pfx\"/>"
      + "<xsl:template match=\"/\"><e p=\"{$pfx}\"><xsl:value-of select=\"count(root/item)\"/></e></xsl:template>"
      + "</xsl:stylesheet>";

    static final String XSL_OUT =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:output method=\"text\" indent=\"yes\"/>"
      + "<xsl:template match=\"/\"><xsl:value-of select=\"name(/*)\"/></xsl:template>"
      + "</xsl:stylesheet>";

    static String norm(String s) { return s.replace("\r", ""); }

    static javax.xml.transform.Templates compile(String xsl) throws Exception {
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        return tf.newTemplates(new javax.xml.transform.stream.StreamSource(
            new java.io.StringReader(xsl)));
    }

    static String run(javax.xml.transform.Templates tpl, String pfx) throws Exception {
        javax.xml.transform.Transformer t = tpl.newTransformer();
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        if (pfx != null) t.setParameter("pfx", pfx);
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                    new javax.xml.transform.stream.StreamResult(sw));
        return norm(sw.toString());
    }

    static void testReuseIsolation() throws Exception {
        javax.xml.transform.Templates tpl = compile(XSL_PARAM);
        javax.xml.transform.Transformer a = tpl.newTransformer();
        javax.xml.transform.Transformer b = tpl.newTransformer();
        check(a != null && b != null && a != b,
              "Templates.newTransformer returns distinct fresh transformers");
        a.setParameter("pfx", "AA");
        b.setParameter("pfx", "BB");
        check("AA".equals(a.getParameter("pfx")) && "BB".equals(b.getParameter("pfx")),
              "two transformers from one Templates keep independent params");
        check(run(tpl, "AA").equals("<e p=\"AA\">3</e>"), "transformer from Templates with param AA");
        check(run(tpl, "BB").equals("<e p=\"BB\">3</e>"), "transformer from Templates with param BB");
        check(run(tpl, null).equals("<e p=\"\">3</e>"),
              "unset param transformer falls back to stylesheet default (no leakage)");
        // deterministic repeated use
        javax.xml.transform.Templates t2 = compile(XSL_PARAM);
        check(run(t2, "X").equals(run(t2, "X")), "repeated newTransformer runs identical");
    }

    static void testOutputPropertiesSnapshot() throws Exception {
        javax.xml.transform.Templates tpl = compile(XSL_OUT);
        java.util.Properties p = tpl.getOutputProperties();
        check("text".equals(p.getProperty(javax.xml.transform.OutputKeys.METHOD)),
              "stylesheet xsl:output method visible in getOutputProperties");
        check("yes".equals(p.getProperty(javax.xml.transform.OutputKeys.INDENT)),
              "stylesheet xsl:output indent visible in getOutputProperties");
        p.setProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
        check("text".equals(tpl.getOutputProperties().getProperty(javax.xml.transform.OutputKeys.METHOD)),
              "getOutputProperties returns a fresh copy each call");
        javax.xml.transform.Transformer t = tpl.newTransformer();
        check("text".equals(t.getOutputProperty(javax.xml.transform.OutputKeys.METHOD)),
              "mutating the snapshot does not affect later newTransformer");
        check(tpl.getOutputProperties() != null, "getOutputProperties still available");
    }
}
