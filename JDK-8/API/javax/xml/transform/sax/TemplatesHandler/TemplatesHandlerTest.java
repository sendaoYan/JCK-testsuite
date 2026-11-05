/*
 * @test
 * @summary javax.xml.transform.sax.TemplatesHandler conformance: interface hierarchy and methods.
 * @run main TemplatesHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class TemplatesHandlerTest {
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
        Class<?> c = Class.forName("javax.xml.transform.sax.TemplatesHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.ContentHandler"), "implements org.xml.sax.ContentHandler");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getTemplates"), "has method getTemplates");
        check(names.contains("setSystemId"), "has method setSystemId");
        check(names.contains("getSystemId"), "has method getSystemId");
        testCompileClosure();
        testSystemIdLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: real SAX-fed stylesheet compilation closure
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static final String XSL_ITEM =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:template match=\"item\"><u id=\"{@id}\"/></xsl:template>"
      + "</xsl:stylesheet>";

    static javax.xml.transform.sax.TemplatesHandler feed(String xsl) throws Exception {
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.sax.TemplatesHandler thh = stf.newTemplatesHandler();
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setNamespaceAware(true); // required so xsl: elements arrive with their namespace
        org.xml.sax.XMLReader pr = spf.newSAXParser().getXMLReader();
        pr.setContentHandler(thh);
        pr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xsl)));
        return thh;
    }

    static void testCompileClosure() throws Exception {
        javax.xml.transform.sax.TemplatesHandler thh = feed(XSL_ITEM);
        javax.xml.transform.Templates tpl = thh.getTemplates();
        check(tpl != null, "getTemplates non-null after SAX-fed stylesheet parse");
        javax.xml.transform.Transformer t = tpl.newTransformer();
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                    new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().replace("\r", "").equals("<u id=\"3\"/><u id=\"1\"/><u id=\"2\"/>"),
              "compiled Templates produce exact output");
        // non-stylesheet input: handler still yields a (default-rules) Templates object;
        // recorded per-javap-confirmed behavior on both JDK-8 and JDK-27
        javax.xml.transform.sax.TemplatesHandler bad = feed("<not-a-stylesheet/>");
        check(bad.getTemplates() != null,
              "non-stylesheet SAX input -> getTemplates still non-null (default-rules templates)");
    }

    static void testSystemIdLifecycle() throws Exception {
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.sax.TemplatesHandler thh = stf.newTemplatesHandler();
        thh.setSystemId("http://base/main.xsl");
        check("http://base/main.xsl".equals(thh.getSystemId()),
              "setSystemId/getSystemId round-trip before parse");
        // parsing with a systemId-bearing InputSource records it on the handler
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        org.xml.sax.XMLReader pr = spf.newSAXParser().getXMLReader();
        pr.setContentHandler(thh);
        org.xml.sax.InputSource is =
            new org.xml.sax.InputSource(new java.io.StringReader(XSL_ITEM));
        is.setSystemId("http://base/auto.xsl");
        pr.parse(is);
        check("http://base/auto.xsl".equals(thh.getSystemId()),
              "handler systemId auto-recorded from parsed InputSource");
        check(thh.getTemplates() != null, "stylesheet compiled after systemId parse");
    }
}
