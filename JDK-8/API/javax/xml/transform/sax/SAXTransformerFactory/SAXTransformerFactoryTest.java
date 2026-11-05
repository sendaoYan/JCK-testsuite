/*
 * @test
 * @summary javax.xml.transform.sax.SAXTransformerFactory conformance: SAX transformer factory.
 * @run main SAXTransformerFactoryTest
 */


public class SAXTransformerFactoryTest {
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
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        check(tf instanceof javax.xml.transform.sax.SAXTransformerFactory, "is SAXTransformerFactory");
        javax.xml.transform.sax.SAXTransformerFactory stf = (javax.xml.transform.sax.SAXTransformerFactory) tf;
        javax.xml.transform.sax.TransformerHandler th = stf.newTransformerHandler();
        check(th != null, "transformer handler non-null");
        testConstantsAndFeatures();
        testTransformerHandlerClosures();
        testTemplatesHandlerClosure();
        testXmlFilterClosure();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: constants + handler/filter pipeline closures
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static final String XSL_ITEM =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:template match=\"item\"><u id=\"{@id}\"/></xsl:template>"
      + "</xsl:stylesheet>";

    static String norm(String s) { return s.replace("\r", ""); }

    static org.xml.sax.Attributes attr(String n, String v) {
        org.xml.sax.helpers.AttributesImpl ai = new org.xml.sax.helpers.AttributesImpl();
        ai.addAttribute("", n, n, "CDATA", v);
        return ai;
    }

    static void testConstantsAndFeatures() throws Exception {
        check("http://javax.xml.transform.sax.SAXTransformerFactory/feature"
                  .equals(javax.xml.transform.sax.SAXTransformerFactory.FEATURE),
              "FEATURE constant value");
        check("http://javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter"
                  .equals(javax.xml.transform.sax.SAXTransformerFactory.FEATURE_XMLFILTER),
              "FEATURE_XMLFILTER constant value");
        javax.xml.transform.sax.SAXTransformerFactory f =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        check(f.getFeature(javax.xml.transform.sax.SAXTransformerFactory.FEATURE),
              "built-in factory reports FEATURE=true");
        check(f.getFeature(javax.xml.transform.sax.SAXTransformerFactory.FEATURE_XMLFILTER),
              "built-in factory reports FEATURE_XMLFILTER=true");
    }

    static void testTransformerHandlerClosures() throws Exception {
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        // handler(Templates): SAX events in, transformed events out
        javax.xml.transform.Templates tpl = stf.newTemplates(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSL_ITEM)));
        javax.xml.transform.sax.TransformerHandler h1 = stf.newTransformerHandler(tpl);
        java.io.StringWriter sw = new java.io.StringWriter();
        h1.setResult(new javax.xml.transform.stream.StreamResult(sw));
        h1.startDocument();
        h1.startElement("", "root", "root", new org.xml.sax.helpers.AttributesImpl());
        h1.startElement("", "item", "item", attr("id", "9"));
        h1.endElement("", "item", "item");
        h1.startElement("", "item", "item", attr("id", "7"));
        h1.endElement("", "item", "item");
        h1.endElement("", "root", "root");
        h1.endDocument();
        check(norm(sw.toString()).endsWith("<u id=\"9\"/><u id=\"7\"/>"),
              "TransformerHandler(Templates) transforms fed SAX events (exact tail)");
        check(h1.getTransformer() != null, "TransformerHandler.getTransformer non-null");
        // handler(Source) + parameter injection through getTransformer()
        javax.xml.transform.sax.TransformerHandler h2 = stf.newTransformerHandler(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
              + "<xsl:param name=\"tag\"/><xsl:template match=\"/\"><w p=\"{$tag}\"/></xsl:template>"
              + "</xsl:stylesheet>")));
        java.io.StringWriter sw2 = new java.io.StringWriter();
        h2.setResult(new javax.xml.transform.stream.StreamResult(sw2));
        h2.getTransformer().setParameter("tag", "injected");
        h2.startDocument();
        h2.startElement("", "root", "root", new org.xml.sax.helpers.AttributesImpl());
        h2.endElement("", "root", "root");
        h2.endDocument();
        check(norm(sw2.toString()).endsWith("<w p=\"injected\"/>"),
              "TransformerHandler(Source) honors setParameter via getTransformer()");
        // identity handler
        javax.xml.transform.sax.TransformerHandler h3 = stf.newTransformerHandler();
        java.io.StringWriter sw3 = new java.io.StringWriter();
        h3.setResult(new javax.xml.transform.stream.StreamResult(sw3));
        h3.startDocument();
        h3.startElement("", "r", "r", new org.xml.sax.helpers.AttributesImpl());
        h3.characters("txt".toCharArray(), 0, 3);
        h3.endElement("", "r", "r");
        h3.endDocument();
        check(norm(sw3.toString()).contains("<r>txt</r>"),
              "identity TransformerHandler copies fed events");
    }

    static void testTemplatesHandlerClosure() throws Exception {
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.sax.TemplatesHandler thh = stf.newTemplatesHandler();
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        org.xml.sax.XMLReader pr = spf.newSAXParser().getXMLReader();
        pr.setContentHandler(thh);
        pr.parse(new org.xml.sax.InputSource(new java.io.StringReader(XSL_ITEM)));
        javax.xml.transform.Templates tpl = thh.getTemplates();
        check(tpl != null, "TemplatesHandler compiles stylesheet fed as SAX events");
        javax.xml.transform.Transformer t = tpl.newTransformer();
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                    new javax.xml.transform.stream.StreamResult(sw));
        check(norm(sw.toString()).equals("<u id=\"3\"/><u id=\"1\"/><u id=\"2\"/>"),
              "compiled Templates transform real input (exact)");
    }

    static void testXmlFilterClosure() throws Exception {
        javax.xml.transform.sax.SAXTransformerFactory stf =
            (javax.xml.transform.sax.SAXTransformerFactory) javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Templates tpl = stf.newTemplates(
            new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSL_ITEM)));
        org.xml.sax.XMLFilter xf = stf.newXMLFilter(tpl);
        check(xf != null, "newXMLFilter(Templates) non-null");
        check(stf.newXMLFilter(
                  new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XSL_ITEM)))
                  != null,
              "newXMLFilter(Source) non-null");
        final java.util.List<String> fev = new java.util.ArrayList<String>();
        xf.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startDocument() { fev.add("SD"); }
            public void startElement(String u, String l, String q, org.xml.sax.Attributes at) {
                fev.add("SE:" + q + "[" + at.getValue("id") + "]");
            }
            public void endElement(String u, String l, String q) { fev.add("EE:" + q); }
        });
        xf.parse(new org.xml.sax.InputSource(new java.io.StringReader(DOC)));
        java.util.List<String> expected = java.util.Arrays.asList(
            "SD", "SE:u[3]", "EE:u", "SE:u[1]", "EE:u", "SE:u[2]", "EE:u");
        check(fev.equals(expected), "XMLFilter emits transformed SAX events (exact sequence)");
    }
}
