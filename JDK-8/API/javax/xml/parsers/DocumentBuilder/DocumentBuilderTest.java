/*
 * @test
 * @summary javax.xml.parsers.DocumentBuilder conformance: parse overloads, null semantics, malformed XML exception, reset, DOM tree production.
 * @run main DocumentBuilderTest
 */

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DocumentBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }

    static final String XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<catalog xmlns:p=\"urn:parts\"><book id=\"b1\" lang=\"en\">" +
        "<title>T1</title><p:price currency=\"USD\">9.99</p:price>" +
        "</book><book id=\"b2\"><title>T2</title></book></catalog>";

    static InputStream xmlStream() throws Exception {
        return new ByteArrayInputStream(XML.getBytes("UTF-8"));
    }

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder db = f.newDocumentBuilder();

        // ---- documented defaults ----
        check(!db.isValidating(), "isValidating default false");
        check(db.isNamespaceAware(), "namespace-aware builder from namespace-aware factory");
        check(!db.isXIncludeAware(), "isXIncludeAware default false");
        check(db.getSchema() == null, "schema default null");

        // ---- parse(InputStream) ----
        Document d = db.parse(xmlStream());
        check("catalog".equals(d.getDocumentElement().getNodeName()), "root element name");
        NodeList books = d.getElementsByTagName("book");
        check(books.getLength() == 2, "two books");
        Element b1 = (Element) books.item(0);
        check("b1".equals(b1.getAttribute("id")), "attribute id");
        check("en".equals(b1.getAttribute("lang")), "attribute lang");
        check("T1".equals(b1.getElementsByTagName("title").item(0).getTextContent()), "nested title");
        // namespace element reached via getElementsByTagNameNS
        NodeList prices = d.getElementsByTagNameNS("urn:parts", "price");
        check(prices.getLength() == 1, "namespace-aware price element");
        check("9.99".equals(prices.item(0).getTextContent()), "price text");
        check("USD".equals(((Element) prices.item(0)).getAttribute("currency")), "price currency");

        // ---- parse(InputStream, systemId) ----
        Document d2 = db.parse(xmlStream(), "http://example.invalid/doc.xml");
        check("catalog".equals(d2.getDocumentElement().getNodeName()), "parse(InputStream, systemId)");

        // ---- parse(InputSource) ----
        Document d3 = db.parse(new InputSource(xmlStream()));
        check("catalog".equals(d3.getDocumentElement().getNodeName()), "parse(InputSource)");

        // ---- parse(String uri) with file: URI works against a temp file (deterministic, self-cleaned) ----
        java.io.File tmp = java.io.File.createTempFile("dbtest", ".xml");
        tmp.deleteOnExit();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp);
        fos.write(XML.getBytes("UTF-8"));
        fos.close();
        Document d4 = db.parse(tmp.toURI().toString());
        check("catalog".equals(d4.getDocumentElement().getNodeName()), "parse(String uri) file: URI");
        Document d5 = db.parse(tmp);
        check("catalog".equals(d5.getDocumentElement().getNodeName()), "parse(File)");
        tmp.delete();

        // ---- null semantics ----
        check(isIae(() -> db.parse((InputStream) null)), "parse((InputStream)null) -> IAE");
        check(isIae(() -> db.parse((java.io.File) null)), "parse((File)null) -> IAE");
        check(isIae(() -> db.parse((String) null)), "parse((String)null) -> IAE");
        check(isIae(() -> db.parse((InputSource) null)), "parse((InputSource)null) -> IAE");

        // ---- malformed XML -> SAXParseException with position ----
        boolean ok = false;
        try {
            db.parse(new ByteArrayInputStream("<a><b></a>".getBytes("UTF-8")));
        } catch (SAXParseException e) {
            ok = e.getLineNumber() > 0 && e.getColumnNumber() > 0 && e.getSystemId() == null;
        }
        check(ok, "malformed XML -> SAXParseException with line/column");

        // ---- empty document, usable ----
        Document empty = db.newDocument();
        check(empty != null, "newDocument non-null");
        check(empty.getDocumentElement() == null, "newDocument has no document element");
        check(empty.getChildNodes().getLength() == 0, "newDocument has no children");
        check(empty.createElement("e") != null, "newDocument createElement usable");
        check(db.getDOMImplementation() != null, "getDOMImplementation non-null");
        check(db.getDOMImplementation().hasFeature("XML", "3.0"), "DOMImplementation hasFeature XML 3.0");

        // ---- reset keeps parser reusable ----
        db.setEntityResolver(null);
        db.setErrorHandler(null);
        db.reset();
        Document d6 = db.parse(xmlStream());
        check("catalog".equals(d6.getDocumentElement().getNodeName()), "parser reusable after reset");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
