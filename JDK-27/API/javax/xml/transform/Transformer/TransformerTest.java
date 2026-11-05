/*
 * @test
 * @summary javax.xml.transform.Transformer conformance: identity transform round-trip, parameters, output properties, null/exception semantics, reset.
 * @run main TransformerTest
 */

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

public class TransformerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isTransformerException(Thrower t) {
        try { t.run(); return false; }
        catch (TransformerException e) { return true; }
        catch (Exception e) { return false; }
    }

    static final String XML = "<root><a x=\"1\">text</a></root>";

    public static void main(String[] args) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        // silence default error listener noise
        final ErrorListener quiet = new ErrorListener() {
            public void warning(TransformerException e) { }
            public void error(TransformerException e) { }
            public void fatalError(TransformerException e) { }
        };
        tf.setErrorListener(quiet);
        Transformer tr = tf.newTransformer();
        tr.setErrorListener(quiet);

        // ---- identity transform round-trip (StreamSource -> StreamResult) ----
        StringWriter sw = new StringWriter();
        tr.transform(new StreamSource(new StringReader(XML)), new StreamResult(sw));
        String out = sw.toString();
        check(out.contains("<?xml version=\"1.0\""), "identity transform emits xml declaration");
        check(out.contains("<root>"), "identity transform preserves root");
        check(out.contains("<a x=\"1\">text</a>") || out.contains("<a x=\"1\">"), "identity transform preserves element/attr/text");

        // ---- null source -> TransformerException ----
        check(isTransformerException(() -> tr.transform(null, new StreamResult(new StringWriter()))),
              "transform(null source) -> TransformerException");

        // ---- parameters ----
        tr.setParameter("p1", "v1");
        check("v1".equals(tr.getParameter("p1")), "setParameter/getParameter");
        check(tr.getParameter("missing") == null, "getParameter(missing) null");
        check(isIae(() -> tr.setParameter("p1", null)), "setParameter(name, null) -> IAE");
        tr.setParameter("p2", "v2");
        tr.clearParameters();
        check(tr.getParameter("p2") == null, "clearParameters clears all");

        // ---- output properties ----
        check("xml".equals(tr.getOutputProperty(OutputKeys.METHOD)), "default output method xml");
        tr.setOutputProperty(OutputKeys.METHOD, "text");
        check("text".equals(tr.getOutputProperty(OutputKeys.METHOD)), "setOutputProperty/getOutputProperty");
        Properties props = new Properties();
        props.setProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperties(props);
        check("xml".equals(tr.getOutputProperties().getProperty(OutputKeys.METHOD)), "setOutputProperties round-trip");
        check(isNpe(() -> tr.setOutputProperty(null, "v")), "setOutputProperty(null name) -> NPE");
        check(isNpe(() -> tr.getOutputProperty(null)), "getOutputProperty(null name) -> NPE");

        // ---- listeners / resolver ----
        check(isIae(() -> tr.setErrorListener(null)), "setErrorListener(null) -> IAE");
        tr.setErrorListener(quiet);
        check(tr.getErrorListener() == quiet, "setErrorListener/getErrorListener round-trip");
        URIResolver ur = new URIResolver() {
            public javax.xml.transform.Source resolve(String href, String base) { return null; }
        };
        tr.setURIResolver(ur);
        check(tr.getURIResolver() == ur, "setURIResolver/getURIResolver round-trip");

        // ---- reset keeps transformer usable ----
        tr.reset();
        StringWriter sw2 = new StringWriter();
        tr.transform(new StreamSource(new StringReader(XML)), new StreamResult(sw2));
        check(sw2.toString().contains("<root>"), "transformer usable after reset");

        testXsltSamples();
        testOutputBehavior();
        testParamIsolation();
        testIdentityMatrix();
        testResetSemantics();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== deepening: XSLT true-transform closures =====================

    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static final String XSL_A =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:template match=\"/\"><out>"
      + "<xsl:value-of select=\"root/item[1]/@id\"/>-"
      + "<xsl:value-of select=\"root/item[1]/@name\"/>-"
      + "<xsl:value-of select=\"root/item[1]\"/>"
      + "</out></xsl:template></xsl:stylesheet>";

    static final String XSL_B =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:template match=\"/\"><sorted>"
      + "<xsl:for-each select=\"root/item\">"
      + "<xsl:sort select=\"@id\"/>"
      + "<e n=\"{@name}\"><xsl:value-of select=\".\"/></e>"
      + "</xsl:for-each>"
      + "</sorted></xsl:template></xsl:stylesheet>";

    static final String XSL_C =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:template match=\"@*|node()\">"
      + "<xsl:copy><xsl:apply-templates select=\"@*|node()\"/></xsl:copy>"
      + "</xsl:template></xsl:stylesheet>";

    static final String XSL_D =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:template match=\"/\"><d>"
      + "<xsl:variable name=\"n\" select=\"count(root/item)\"/>"
      + "<xsl:if test=\"$n = 3\">three</xsl:if>"
      + "<xsl:if test=\"$n != 3\">other</xsl:if>"
      + "</d></xsl:template></xsl:stylesheet>";

    static final String XSL_E =
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
      + "<xsl:decimal-format name=\"df\" grouping-separator=\",\" decimal-separator=\".\"/>"
      + "<xsl:param name=\"pfx\"/>"
      + "<xsl:template match=\"/\"><e p=\"{$pfx}\">"
      + "<xsl:value-of select=\"format-number(1234.5678, '#,##0.00', 'df')\"/>"
      + "</e></xsl:template></xsl:stylesheet>";

    static String norm(String s) { return s.replace("\r", ""); }

    static String transform(String xsl, String xml) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer(new StreamSource(new StringReader(xsl)));
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sw = new StringWriter();
        t.transform(new StreamSource(new StringReader(xml)), new StreamResult(sw));
        return norm(sw.toString());
    }

    static void testXsltSamples() throws Exception {
        check(transform(XSL_A, DOC).equals("<out>3-gamma-third</out>"),
              "XSLT sample A: value-of/@ selection exact");
        check(transform(XSL_B, DOC).equals(
                  "<sorted><e n=\"alpha\">first</e><e n=\"beta\">second</e><e n=\"gamma\">third</e></sorted>"),
              "XSLT sample B: for-each + xsl:sort order locked");
        check(transform(XSL_C, DOC).equals(DOC),
              "XSLT sample C: apply-templates recursive copy exact");
        check(transform(XSL_D, DOC).equals("<d>three</d>"),
              "XSLT sample D: xsl:variable + xsl:if condition");
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer(new StreamSource(new StringReader(XSL_E)));
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setParameter("pfx", "JX");
        StringWriter sw = new StringWriter();
        t.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sw));
        check(norm(sw.toString()).equals("<e p=\"JX\">1,234.57</e>"),
              "XSLT sample E: setParameter injects $pfx + format-number exact");
        Transformer t0 = tf.newTransformer(new StreamSource(new StringReader(XSL_E)));
        t0.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sw0 = new StringWriter();
        t0.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sw0));
        check(norm(sw0.toString()).equals("<e p=\"\">1,234.57</e>"),
              "XSLT sample E default: param never set -> empty string");
    }

    static void testOutputBehavior() throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        // declaration emitted to a Writer: exact form locked on this JDK
        Transformer t = tf.newTransformer();
        StringWriter sw = new StringWriter();
        t.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sw));
        String decl = sw.toString().substring(0, sw.toString().indexOf("?><") + 2);
        check(decl.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"),
              "identity declaration exact: " + decl);
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        sw = new StringWriter();
        t.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sw));
        check(!sw.toString().contains("<?xml"), "omit-xml-declaration=yes removes declaration");
        Transformer t2 = tf.newTransformer();
        t2.setOutputProperty(OutputKeys.STANDALONE, "yes");
        sw = new StringWriter();
        t2.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sw));
        check(sw.toString().contains("standalone=\"yes\""), "standalone=yes appears in declaration");
        // encoding=UTF-16 into an OutputStream emits a BOM (either byte order)
        Transformer t3 = tf.newTransformer();
        t3.setOutputProperty(OutputKeys.ENCODING, "UTF-16");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        t3.transform(new StreamSource(new StringReader(DOC)), new StreamResult(bos));
        byte[] b = bos.toByteArray();
        boolean bom = (b[0] == (byte) 0xfe && b[1] == (byte) 0xff)
                   || (b[0] == (byte) 0xff && b[1] == (byte) 0xfe);
        check(bom, "encoding=UTF-16 output starts with FEFF/FFFE two-byte BOM");
        check(b.length % 2 == 0, "encoding=UTF-16 output has even byte count");
        // method=text
        Transformer t4 = tf.newTransformer(new StreamSource(new StringReader(
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
          + "<xsl:template match=\"/\"><xsl:value-of select=\"root/item[2]\"/></xsl:template></xsl:stylesheet>")));
        t4.setOutputProperty(OutputKeys.METHOD, "text");
        sw = new StringWriter();
        t4.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sw));
        check(sw.toString().equals("first"), "method=text plain text exact");
        check(!sw.toString().contains("<"), "method=text emits no markup");
        // method=html
        Transformer t5 = tf.newTransformer(new StreamSource(new StringReader(
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
          + "<xsl:template match=\"/\"><html><body>"
          + "<xsl:value-of select=\"root/item[1]/@name\"/><br/><p>a&lt;b &amp; c</p>"
          + "</body></html></xsl:template></xsl:stylesheet>")));
        t5.setOutputProperty(OutputKeys.METHOD, "html");
        sw = new StringWriter();
        t5.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sw));
        String html = norm(sw.toString());
        check(html.contains("<br>") && !html.contains("<br/>"),
              "method=html: empty element not self-closed");
        check(html.contains("gamma"), "method=html: value present");
        check(html.contains("&lt;"), "method=html: text still escaped");
        // cdata-section-elements
        Transformer t6 = tf.newTransformer(new StreamSource(new StringReader(
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
          + "<xsl:template match=\"/\"><e><xsl:value-of select=\"root/item[1]\"/>&lt;tag&gt;</e></xsl:template></xsl:stylesheet>")));
        t6.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "e");
        sw = new StringWriter();
        t6.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sw));
        check(sw.toString().contains("<![CDATA[third<tag>]]>"),
              "cdata-section-elements=e wraps text in CDATA");
        // default output property values
        Transformer id = tf.newTransformer();
        check("xml".equals(id.getOutputProperty(OutputKeys.METHOD)), "default method=xml");
        check("1.0".equals(id.getOutputProperty(OutputKeys.VERSION)), "default version=1.0");
        check("UTF-8".equals(id.getOutputProperty(OutputKeys.ENCODING)), "default encoding=UTF-8");
        check("no".equals(id.getOutputProperty(OutputKeys.OMIT_XML_DECLARATION)),
              "default omit-xml-declaration=no");
        check("no".equals(id.getOutputProperty(OutputKeys.INDENT)), "default indent=no");
        check("no".equals(id.getOutputProperty(OutputKeys.STANDALONE)), "default standalone=no");
        // bogus output key -> IllegalArgumentException naming the key
        boolean iae = false;
        try { id.setOutputProperty("bogus-key", "v"); }
        catch (IllegalArgumentException e) { iae = e.getMessage() != null && e.getMessage().contains("bogus-key"); }
        check(iae, "setOutputProperty(bogus key) -> IAE naming the key");
        // getOutputProperties returns a copy
        Properties snap = id.getOutputProperties();
        snap.setProperty(OutputKeys.METHOD, "text");
        check("xml".equals(id.getOutputProperty(OutputKeys.METHOD)),
              "mutating getOutputProperties copy does not affect transformer");
        // setOutputProperties(null) accepted
        boolean nullOk = false;
        try { id.setOutputProperties(null); nullOk = true; } catch (Exception e) { }
        check(nullOk, "setOutputProperties(null) accepted");
        // null result
        check(isTransformerException(() -> id.transform(new StreamSource(new StringReader(DOC)), null)),
              "transform(source, null) -> TransformerException");
        // same transformer reused -> identical output
        Transformer u = tf.newTransformer();
        u.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter s1 = new StringWriter(), s2 = new StringWriter();
        u.transform(new StreamSource(new StringReader(DOC)), new StreamResult(s1));
        u.transform(new StreamSource(new StringReader(DOC)), new StreamResult(s2));
        check(s1.toString().equals(s2.toString()), "transformer reuse gives identical output");
    }

    static void testParamIsolation() throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer a = tf.newTransformer(new StreamSource(new StringReader(XSL_E)));
        Transformer b = tf.newTransformer(new StreamSource(new StringReader(XSL_E)));
        a.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        b.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        a.setParameter("pfx", "AA");
        b.setParameter("pfx", "BB");
        check("AA".equals(a.getParameter("pfx")) && "BB".equals(b.getParameter("pfx")),
              "two transformers keep independent params");
        StringWriter sa = new StringWriter(), sb = new StringWriter();
        a.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sa));
        b.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sb));
        check(norm(sa.toString()).contains("p=\"AA\""), "transformer A output uses its own param");
        check(norm(sb.toString()).contains("p=\"BB\""), "transformer B output uses its own param");
        a.clearParameters();
        StringWriter sa2 = new StringWriter();
        a.transform(new StreamSource(new StringReader(DOC)), new StreamResult(sa2));
        check(norm(sa2.toString()).contains("p=\"\""), "clearParameters falls back to stylesheet default");
        Object v = Integer.valueOf(42);
        b.setParameter("num", v);
        check(b.getParameter("num") == v, "setParameter non-String round-trips same object");
    }

    static void testIdentityMatrix() throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer id = tf.newTransformer();
        id.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        javax.xml.parsers.DocumentBuilder db =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc =
            db.parse(new java.io.ByteArrayInputStream(DOC.getBytes("UTF-8")));
        // DOMSource -> StreamResult: identity closure
        StringWriter sw = new StringWriter();
        id.transform(new javax.xml.transform.dom.DOMSource(doc), new StreamResult(sw));
        check(norm(sw.toString()).equals(DOC), "identity DOM->Stream exact");
        // StreamSource -> DOMResult: tree assertions
        javax.xml.transform.dom.DOMResult dr = new javax.xml.transform.dom.DOMResult();
        id.transform(new StreamSource(new StringReader(DOC)), dr);
        org.w3c.dom.Node n = dr.getNode();
        check(n != null && n.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE,
              "DOMResult node is a Document");
        org.w3c.dom.Element root = ((org.w3c.dom.Document) n).getDocumentElement();
        check("root".equals(root.getNodeName()), "DOMResult root element name");
        check(root.getChildNodes().getLength() == 3, "DOMResult root has 3 children");
        check("3".equals(root.getFirstChild().getAttributes().getNamedItem("id").getNodeValue()),
              "DOMResult first item @id");
        check("gamma".equals(root.getFirstChild().getAttributes().getNamedItem("name").getNodeValue()),
              "DOMResult first item @name");
        check("third".equals(root.getFirstChild().getTextContent()),
              "DOMResult first item text content");
        // DOM -> DOM
        org.w3c.dom.Document d2 = db.newDocument();
        id.transform(new javax.xml.transform.dom.DOMSource(doc),
                     new javax.xml.transform.dom.DOMResult(d2));
        org.w3c.dom.Element r2 = d2.getDocumentElement();
        check(r2 != null && "root".equals(r2.getNodeName()) && r2.getChildNodes().getLength() == 3,
              "identity DOM->DOM tree shape");
        check("gamma".equals(r2.getFirstChild().getAttributes().getNamedItem("name").getNodeValue()),
              "identity DOM->DOM attribute preserved");
    }

    static void testResetSemantics() throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        check("yes".equals(t.getOutputProperty(OutputKeys.INDENT)), "indent set before reset");
        t.reset();
        check("no".equals(t.getOutputProperty(OutputKeys.INDENT)),
              "reset() clears explicit output property back to default");
        // parameters are cleared by reset() as well
        Transformer tp = tf.newTransformer();
        tp.setParameter("k", "v");
        tp.reset();
        check(tp.getParameter("k") == null, "reset() clears parameters");
        // Version-locked divergence: after setOutputProperties(null) has been called once,
        // JDK-8's reset() restores a snapshot that still carries later explicit sets,
        // JDK-27's reset() always falls back to the pristine defaults.
        Transformer t2 = tf.newTransformer();
        t2.setOutputProperties(null);
        t2.setOutputProperty(OutputKeys.INDENT, "yes");
        t2.reset();
        check("no".equals(t2.getOutputProperty(OutputKeys.INDENT)),
              "JDK-27: reset() clears even after setOutputProperties(null) (JDK-8 keeps yes)");
    }
}
