/*
 * @test
 * @summary javax.xml.transform.sax.TransformerHandler conformance: interface hierarchy and methods.
 * @run main TransformerHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class TransformerHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.transform.sax.TransformerHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.ContentHandler"), "implements org.xml.sax.ContentHandler");
        check(has(ifs, "org.xml.sax.ext.LexicalHandler"), "implements org.xml.sax.ext.LexicalHandler");
        check(has(ifs, "org.xml.sax.DTDHandler"), "implements org.xml.sax.DTDHandler");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setResult"), "has method setResult");
        check(names.contains("setSystemId"), "has method setSystemId");
        check(names.contains("getSystemId"), "has method getSystemId");
        check(names.contains("getTransformer"), "has method getTransformer");
        testPipelineClosure();
        testParamInjectionAndSystemId();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: real SAX-producer -> TransformerHandler -> Result pipeline
    static final String XSL_ITEM =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:template match=\"item\"><u id=\"{@id}\"/></xsl:template>"
      + "</xsl:stylesheet>";

    static String norm(String s) { return s.replace("\r", ""); }

    static javax.xml.transform.sax.TransformerHandler handler(javax.xml.transform.Source xsl)
            throws Exception {
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        return stf.newTransformerHandler(xsl);
    }

    static void testPipelineClosure() throws Exception {
        // handler built from a compiled Templates
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Templates tpl = stf.newTemplates(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSL_ITEM)));
        javax.xml.transform.sax.TransformerHandler th = stf.newTransformerHandler(tpl);
        java.io.StringWriter sw = new java.io.StringWriter();
        th.setResult(new javax.xml.transform.stream.StreamResult(sw));
        org.xml.sax.helpers.AttributesImpl noAt = new org.xml.sax.helpers.AttributesImpl();
        org.xml.sax.helpers.AttributesImpl id9 = new org.xml.sax.helpers.AttributesImpl();
        id9.addAttribute("", "id", "id", "CDATA", "9");
        org.xml.sax.helpers.AttributesImpl id7 = new org.xml.sax.helpers.AttributesImpl();
        id7.addAttribute("", "id", "id", "CDATA", "7");
        th.startDocument();
        th.startElement("", "root", "root", noAt);
        th.startElement("", "item", "item", id9);
        th.endElement("", "item", "item");
        th.startElement("", "item", "item", id7);
        th.endElement("", "item", "item");
        th.endElement("", "root", "root");
        th.endDocument();
        check(norm(sw.toString()).endsWith("<u id=\"9\"/><u id=\"7\"/>"),
              "SAX events fed to handler(Templates) transform to exact output");
        check(th.getTransformer() != null, "getTransformer non-null");
        // handler built from a Source directly
        javax.xml.transform.sax.TransformerHandler th2 =
            handler(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSL_ITEM)));
        java.io.StringWriter sw2 = new java.io.StringWriter();
        th2.setResult(new javax.xml.transform.stream.StreamResult(sw2));
        org.xml.sax.helpers.AttributesImpl id5 = new org.xml.sax.helpers.AttributesImpl();
        id5.addAttribute("", "id", "id", "CDATA", "5");
        th2.startDocument();
        th2.startElement("", "item", "item", id5);
        th2.endElement("", "item", "item");
        th2.endDocument();
        check(norm(sw2.toString()).endsWith("<u id=\"5\"/>"),
              "SAX events fed to handler(Source) transform to exact output");
    }

    static void testParamInjectionAndSystemId() throws Exception {
        javax.xml.transform.sax.TransformerHandler th = handler(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:param name=\"tag\"/><xsl:template match=\"/\"><w p=\"{$tag}\"/></xsl:template>"
              + "</xsl:stylesheet>")));
        java.io.StringWriter sw = new java.io.StringWriter();
        th.setResult(new javax.xml.transform.stream.StreamResult(sw));
        th.getTransformer().setParameter("tag", "injected");
        th.startDocument();
        th.startElement("", "root", "root", new org.xml.sax.helpers.AttributesImpl());
        th.endElement("", "root", "root");
        th.endDocument();
        check(norm(sw.toString()).endsWith("<w p=\"injected\"/>"),
              "setParameter on handler's transformer drives $param in stylesheet");
        th.setSystemId("http://h/sys");
        check("http://h/sys".equals(th.getSystemId()), "setSystemId/getSystemId round-trip");
        // after a completed run, setResult may be swapped and the handler reused
        java.io.StringWriter sw2 = new java.io.StringWriter();
        th.setResult(new javax.xml.transform.stream.StreamResult(sw2));
        th.startDocument();
        th.startElement("", "root", "root", new org.xml.sax.helpers.AttributesImpl());
        th.endElement("", "root", "root");
        th.endDocument();
        check(norm(sw2.toString()).endsWith("<w p=\"injected\"/>"),
              "handler reusable with a new Result after a completed run");
    }
}
